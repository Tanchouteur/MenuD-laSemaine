import type { MealSlotDto } from '@/types/api';

export function displayMeal(slot: MealSlotDto, allSlots: MealSlotDto[]) {
  if (slot.assignment) {
    return {
      name: slot.assignment.name,
      description: slot.assignment.description ?? 'Repas choisi pour la famille.',
      minutes: slot.assignment.totalMinutes,
    };
  }
  if (slot.slotType === 'eating_out') {
    return { name: slot.customLabel || 'Repas à l’extérieur', description: 'Aucune course à prévoir.' };
  }
  if (slot.slotType === 'custom') {
    return { name: slot.customLabel || 'Repas libre', description: 'Choisi directement par la famille.' };
  }
  if (slot.slotType === 'leftovers') {
    const source = allSlots.find((item) => item.id === slot.leftoversFromSlotId);
    return {
      name: `Restes${source?.assignment ? ` de ${source.assignment.name}` : ''}`,
      description: 'Les quantités ne sont pas ajoutées une seconde fois aux courses.',
    };
  }
  return { name: 'À choisir', description: 'Ce créneau sera rempli à la prochaine génération.' };
}
