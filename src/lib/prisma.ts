import { AsyncLocalStorage } from 'node:async_hooks';
import type { Prisma } from '../../generated/prisma/client';
import { PrismaPg } from '@prisma/adapter-pg';
import { PrismaClient } from '../../generated/prisma/client';

const globalForPrisma = globalThis as unknown as {
  prisma?: PrismaClient;
};

const transactionScope = new AsyncLocalStorage<PrismaClient>();

/** Join callback transactions in existing services to the outer atomic operation.
 * No savepoint/independent commit: any error must propagate to the outer owner.
 * Array transactions are deliberately unsupported in this scope.
 */
export function withPrismaTransaction<T>(transaction: Prisma.TransactionClient, work: () => Promise<T>): Promise<T> {
  const scoped = new Proxy(transaction, {
    get(target, property) {
      if (property === '$transaction') return (callback: unknown) => {
        if (typeof callback !== 'function') throw new Error('Scoped transactions require a callback.');
        return callback(transaction);
      };
      const value = Reflect.get(target, property);
      return typeof value === 'function' ? value.bind(target) : value;
    },
  }) as PrismaClient;
  return transactionScope.run(scoped, work);
}

export function getPrisma(): PrismaClient {
  const scoped = transactionScope.getStore();
  if (scoped) return scoped;
  const connectionString = process.env.DATABASE_URL;

  if (!connectionString) {
    throw new Error('DATABASE_URL est requise pour accéder à PostgreSQL.');
  }

  if (!globalForPrisma.prisma) {
    const adapter = new PrismaPg({ connectionString });
    globalForPrisma.prisma = new PrismaClient({ adapter });
  }

  return globalForPrisma.prisma;
}
