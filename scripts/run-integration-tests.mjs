import { randomUUID } from 'node:crypto';
import { spawn } from 'node:child_process';

const root = new URL('..', import.meta.url).pathname;
const containerName = `menu-tests-${randomUUID().slice(0, 8)}`;
let ownsContainer = false;

function run(command, args, options = {}) {
  return new Promise((resolve, reject) => {
    const child = spawn(command, args, {
      cwd: root,
      env: { ...process.env, ...options.env },
      stdio: options.capture ? ['ignore', 'pipe', 'pipe'] : 'inherit',
    });
    let stdout = '';
    let stderr = '';
    child.stdout?.on('data', (chunk) => { stdout += chunk; });
    child.stderr?.on('data', (chunk) => { stderr += chunk; });
    child.on('error', reject);
    child.on('exit', (code) => {
      if (code === 0) resolve(stdout.trim());
      else reject(new Error(`${command} ${args.join(' ')} a échoué (${code}).\n${stderr}`));
    });
  });
}

function assertSafeTestDatabase(connectionString) {
  const url = new URL(connectionString);
  const localHost = url.hostname === '127.0.0.1' || url.hostname === 'localhost';
  const testDatabase = url.pathname.toLowerCase().includes('test');
  if (!localHost || !testDatabase) {
    throw new Error(
      'Refus de lancer les tests : la base doit être locale et son nom doit contenir "test".',
    );
  }
}

async function stopContainer() {
  if (!ownsContainer) return;
  ownsContainer = false;
  await run('docker', ['rm', '--force', containerName], { capture: true }).catch(() => {});
}

process.once('SIGINT', async () => {
  await stopContainer();
  process.exit(130);
});
process.once('SIGTERM', async () => {
  await stopContainer();
  process.exit(143);
});

let databaseUrl = process.env.TEST_DATABASE_URL;

try {
  if (!databaseUrl) {
    await run('docker', [
      'run', '--detach', '--rm',
      '--name', containerName,
      '--env', 'POSTGRES_DB=menu_test',
      '--env', 'POSTGRES_USER=menu_test',
      '--env', 'POSTGRES_PASSWORD=menu_test',
      '--publish', '127.0.0.1::5432',
      'postgres:17-alpine',
    ], { capture: true });
    ownsContainer = true;

    const portOutput = await run('docker', ['port', containerName, '5432/tcp'], { capture: true });
    const port = portOutput.match(/:(\d+)$/)?.[1];
    if (!port) throw new Error(`Port PostgreSQL introuvable dans : ${portOutput}`);
    databaseUrl = `postgresql://menu_test:menu_test@127.0.0.1:${port}/menu_test?schema=public`;

    let ready = false;
    for (let attempt = 0; attempt < 40; attempt += 1) {
      try {
        await run('docker', ['exec', containerName, 'pg_isready', '-U', 'menu_test', '-d', 'menu_test'], { capture: true });
        ready = true;
        break;
      } catch {
        await new Promise((resolve) => setTimeout(resolve, 250));
      }
    }
    if (!ready) throw new Error('PostgreSQL 17 n’est pas devenu disponible à temps.');
  }

  assertSafeTestDatabase(databaseUrl);
  const testEnv = { DATABASE_URL: databaseUrl, NODE_ENV: 'test' };
  await run('./node_modules/.bin/prisma', ['migrate', 'deploy'], { env: testEnv });
  await run('./node_modules/.bin/vitest', ['run', '--config', 'vitest.integration.config.ts'], { env: testEnv });
} finally {
  await stopContainer();
}
