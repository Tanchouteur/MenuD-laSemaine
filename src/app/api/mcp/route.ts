import { WebStandardStreamableHTTPServerTransport } from '@modelcontextprotocol/server';
import { authenticate } from '@/mcp/auth';
import { McpFault, failure } from '@/mcp/errors';
import { createServer } from '@/mcp/server';

export const runtime = 'nodejs';
export const dynamic = 'force-dynamic';

export async function POST(request: Request): Promise<Response> {
  let server: ReturnType<typeof createServer> | undefined;
  try {
    const configured = process.env.MCP_PUBLIC_URL;
    if (!configured) throw new McpFault('MCP_NOT_CONFIGURED');
    const url = new URL(configured);
    if (url.pathname !== '/api/mcp' || url.username || url.password || url.search || url.hash ||
      (url.protocol !== 'https:' && !(url.protocol === 'http:' && ['localhost', '127.0.0.1'].includes(url.hostname)))) throw new McpFault('MCP_NOT_CONFIGURED');
    // Never trust forwarded headers to extend the configured host/origin allowlist.
    if ((request.headers.get('host') ?? new URL(request.url).host) !== url.host ||
      (request.headers.has('origin') && request.headers.get('origin') !== url.origin)) throw new McpFault('ORIGIN_REJECTED');
    const credentialId = await authenticate(request);
    server = createServer(credentialId);
    const transport = new WebStandardStreamableHTTPServerTransport({
      sessionIdGenerator: undefined, enableJsonResponse: true, maxRequestBodySize: 262144,
    });
    await server.connect(transport);
    const response = await transport.handleRequest(request);
    response.headers.set('Cache-Control', 'no-store');
    return response;
  } catch (error) {
    const code = error instanceof McpFault ? error.code : 'INTERNAL_ERROR';
    const status = code === 'UNAUTHENTICATED' ? 401 : code === 'ORIGIN_REJECTED' ? 403 : 503;
    const headers: Record<string, string> = { 'Cache-Control': 'no-store' };
    if (status === 401) headers['WWW-Authenticate'] = 'Bearer realm="menu-mcp"';
    return Response.json(error instanceof McpFault ? failure(error) : failure(new McpFault('INTERNAL_ERROR', {}, true)), { status, headers });
  } finally {
    // JSON responses are complete when handleRequest resolves. No in-memory session.
    await server?.close();
  }
}
function unsupported() { return Response.json(failure(new McpFault('METHOD_NOT_ALLOWED')), { status: 405, headers: { Allow: 'POST', 'Cache-Control': 'no-store' } }); }
export const GET = unsupported;
export const DELETE = unsupported;
