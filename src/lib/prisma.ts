import { PrismaPg } from '@prisma/adapter-pg';
import { PrismaClient } from '../../generated/prisma/client';

const globalForPrisma = globalThis as unknown as {
  prisma?: PrismaClient;
};

export function getPrisma(): PrismaClient {
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
