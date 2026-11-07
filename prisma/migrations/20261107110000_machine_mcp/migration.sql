CREATE TABLE "MachineCredential" (
  "id" TEXT PRIMARY KEY, "tokenHash" TEXT NOT NULL UNIQUE,
  "scopes" TEXT[] NOT NULL, "revokedAt" TIMESTAMP(3),
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE "MachineOperation" (
  "id" TEXT PRIMARY KEY, "credentialId" TEXT NOT NULL,
  "key" TEXT NOT NULL, "tool" TEXT NOT NULL, "requestHash" TEXT NOT NULL,
  "result" JSONB NOT NULL, "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT "MachineOperation_credentialId_fkey" FOREIGN KEY ("credentialId")
    REFERENCES "MachineCredential"("id") ON DELETE RESTRICT ON UPDATE CASCADE
);
CREATE UNIQUE INDEX "MachineOperation_credentialId_key_key" ON "MachineOperation"("credentialId", "key");
