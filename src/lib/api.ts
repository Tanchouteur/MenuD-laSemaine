export function apiError(error: unknown): Response {
  const message = error instanceof Error ? error.message : 'Une erreur inattendue est survenue.';
  const status = /introuvable|n’existe|n’est plus disponible/i.test(message) ? 404 : 400;
  return Response.json({ error: message }, { status });
}

export async function readJson(request: Request): Promise<unknown> {
  try {
    return await request.json();
  } catch {
    throw new Error('Le contenu de la requête est invalide.');
  }
}
