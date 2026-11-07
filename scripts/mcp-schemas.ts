import { writeFileSync } from 'node:fs';
import { z } from 'zod';
import { schemas, type ToolName } from '../src/mcp/contracts';
import { resultSchemas } from '../src/mcp/results';
import { descriptions } from '../src/mcp/descriptions';
writeFileSync('docs/MCP.schemas.json', JSON.stringify(Object.fromEntries((Object.keys(schemas) as ToolName[]).map(name => [name, {
  description: descriptions[name],
  inputSchema: z.toJSONSchema(schemas[name]),
  outputSchema: z.toJSONSchema(resultSchemas[name]),
}])), null, 2) + '\n');
