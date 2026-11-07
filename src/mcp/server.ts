import { McpServer } from '@modelcontextprotocol/server';
import { descriptions } from './descriptions';
import { resultSchemas } from './results';
import { schemas, writeTools, type ToolName } from './contracts';
import { executeTool } from './tools';

export function createServer(credentialId: string) {
  const server = new McpServer({ name: 'menu-de-la-semaine', version: '1.0.0' });
  for (const name of Object.keys(schemas) as ToolName[]) {
    server.registerTool(name, {
      description: descriptions[name],
      inputSchema: schemas[name],
      outputSchema: resultSchemas[name],
      annotations: { readOnlyHint: !writeTools.has(name), destructiveHint: name === 'apply_week_import', idempotentHint: true, openWorldHint: false },
    }, async (args: unknown) => {
      const result = await executeTool(name, args, credentialId);
      const structuredContent = JSON.parse(JSON.stringify(result)) as Record<string, unknown>;
      return { content: [{ type: 'text' as const, text: JSON.stringify(structuredContent) }], structuredContent, isError: structuredContent.ok === false };
    });
  }
  return server;
}
