import type { RandomSource, ScoredCandidate } from './types';

export function weightedPick(
  candidates: readonly ScoredCandidate[],
  random: RandomSource,
): ScoredCandidate | null {
  const eligible = candidates.filter((candidate) => candidate.score > 0);
  const total = eligible.reduce((sum, candidate) => sum + candidate.score, 0);

  if (total <= 0) return null;

  const target = random.next() * total;
  let cumulative = 0;

  for (const candidate of eligible) {
    cumulative += candidate.score;
    if (cumulative > target) return candidate;
  }

  return eligible.at(-1) ?? null;
}

export function weightedPickMany(
  candidates: readonly ScoredCandidate[],
  count: number,
  random: RandomSource,
): ScoredCandidate[] {
  const remaining = [...candidates];
  const picked: ScoredCandidate[] = [];

  while (picked.length < count && remaining.length > 0) {
    const candidate = weightedPick(remaining, random);
    if (!candidate) break;
    picked.push(candidate);
    remaining.splice(remaining.indexOf(candidate), 1);
  }

  return picked;
}
