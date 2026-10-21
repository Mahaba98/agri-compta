export type UserRole = 'ADMIN' | 'AGRICULTEUR' | 'COMPTABLE';
export type Section = 'dashboard' | 'exploitations' | 'campagnes' | 'parcelles' | 'cultures' | 'operations' | 'recoltes' | 'depenses' | 'recettes' | 'stocks' | 'rapports' | 'utilisateurs';

const agriculture: Section[] = ['campagnes', 'parcelles', 'cultures', 'operations', 'recoltes', 'stocks'];

export function canViewSection(role: UserRole | undefined, section: Section): boolean {
  return !!role && (section !== 'utilisateurs' || role === 'ADMIN');
}

export function canCreateSection(role: UserRole | undefined, section: Section): boolean {
  if (section === 'utilisateurs' || section === 'exploitations') return role === 'ADMIN';
  if (agriculture.includes(section)) return role === 'AGRICULTEUR';
  if (section === 'depenses' || section === 'recettes') return role === 'AGRICULTEUR' || role === 'COMPTABLE';
  return false;
}

export function canEditSection(role: UserRole | undefined, section: Section): boolean {
  if (section === 'depenses' || section === 'recettes') return role === 'COMPTABLE';
  return canCreateSection(role, section);
}

export function canCancelFinancial(role: UserRole | undefined, section: Section): boolean {
  return role === 'COMPTABLE' && (section === 'depenses' || section === 'recettes');
}
