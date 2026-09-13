import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

type Section = 'dashboard' | 'campagnes' | 'parcelles' | 'cultures' | 'operations' | 'recoltes' | 'depenses' | 'recettes' | 'stocks' | 'rapports' | 'utilisateurs';
type ViewMode = 'list' | 'create' | 'detail' | 'edit';
type StockFormKind = 'produit' | 'mouvement';
type UserRole = 'ADMIN' | 'AGRICULTEUR' | 'COMPTABLE';

interface Entity {
  id: number;
  [key: string]: unknown;
}

interface Dashboard {
  totalDepenses: number;
  totalOperations: number;
  totalRecettes: number;
  beneficeGlobal: number;
  rentabilites: RentabiliteCulture[];
  stocksFaibles: ProduitStock[];
}

interface RentabiliteCulture {
  cultureId: number;
  culture: string;
  surfaceHa: number;
  totalDepenses: number;
  totalOperations: number;
  coutTotal: number;
  recetteTotale: number;
  benefice: number;
  coutParHectare: number;
  margeParHectare: number;
}

interface ProduitStock extends Entity {
  nom: string;
  typeProduit: string;
  unite: string;
  quantiteDisponible: number;
  seuilAlerte: number;
  prixUnitaireMoyen?: number;
}

interface ListColumn {
  label: string;
  value: (item: Entity) => string;
}

interface AuthUser {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: UserRole;
}

interface LoginResponse {
  token: string;
  utilisateur: AuthUser;
}

interface TokenPayload {
  utilisateurId: number;
  role: UserRole;
  email: string;
  expiresAt: number;
}

interface RapportCulture {
  cultureId: number;
  culture: string;
  campagne: string;
  parcelle: string;
  surfaceHa: number;
  totalDepenses: number;
  totalOperations: number;
  totalRecettes: number;
  benefice: number;
  quantiteRecoltee: number;
  unite: string;
  nombreOperations: number;
  nombreRecoltes: number;
}

interface RapportSynthese {
  dateDebut?: string;
  dateFin?: string;
  campagneId?: number;
  cultureId?: number;
  totalDepenses: number;
  totalOperations: number;
  totalRecettes: number;
  benefice: number;
  totalQuantiteRecoltee: number;
  nombreOperations: number;
  nombreRecoltes: number;
  valeurStock: number;
  nombreStocksFaibles: number;
  cultures: RapportCulture[];
}

@Component({
  selector: 'app-root',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly api = 'http://localhost:8080/api';

  readonly section = signal<Section>('dashboard');
  readonly mode = signal<ViewMode>('list');
  readonly stockFormKind = signal<StockFormKind>('produit');
  readonly selected = signal<Entity | null>(null);
  readonly searchTerm = signal('');
  readonly pageIndex = signal(1);
  readonly pageSize = signal(10);
  readonly loading = signal(false);
  readonly message = signal('');
  readonly error = signal('');
  readonly authToken = signal<string | null>(this.readStoredToken());
  readonly currentUser = signal<AuthUser | null>(this.readStoredUser());

  readonly dashboard = signal<Dashboard | null>(null);
  readonly rapport = signal<RapportSynthese | null>(null);
  readonly categories = signal<Entity[]>([]);
  readonly utilisateurs = signal<Entity[]>([]);
  readonly campagnes = signal<Entity[]>([]);
  readonly parcelles = signal<Entity[]>([]);
  readonly cultures = signal<Entity[]>([]);
  readonly operations = signal<Entity[]>([]);
  readonly recoltes = signal<Entity[]>([]);
  readonly depenses = signal<Entity[]>([]);
  readonly recettes = signal<Entity[]>([]);
  readonly produits = signal<ProduitStock[]>([]);

  readonly navItems: { id: Section; label: string; icon: string }[] = [
    { id: 'dashboard', label: 'Tableau de bord', icon: 'layout-dashboard' },
    { id: 'campagnes', label: 'Campagnes', icon: 'calendar-days' },
    { id: 'parcelles', label: 'Parcelles', icon: 'map-pinned' },
    { id: 'cultures', label: 'Cultures', icon: 'sprout' },
    { id: 'operations', label: 'Opérations', icon: 'tractor' },
    { id: 'recoltes', label: 'Récoltes', icon: 'wheat' },
    { id: 'depenses', label: 'Dépenses', icon: 'wallet' },
    { id: 'recettes', label: 'Recettes', icon: 'receipt' },
    { id: 'stocks', label: 'Stocks', icon: 'package' },
    { id: 'rapports', label: 'Rapports', icon: 'list' },
    { id: 'utilisateurs', label: 'Utilisateurs', icon: 'layers' },
  ];

  readonly uniteOptions = [
    { value: 'KG', label: 'Kilogramme (kg)' },
    { value: 'G', label: 'Gramme (g)' },
    { value: 'T', label: 'Tonne (t)' },
    { value: 'L', label: 'Litre (l)' },
    { value: 'ML', label: 'Millilitre (ml)' },
    { value: 'SAC', label: 'Sac' },
    { value: 'BIDON', label: 'Bidon' },
    { value: 'BOTTE', label: 'Botte' },
    { value: 'CAISSE', label: 'Caisse' },
    { value: 'UNITE', label: 'Unite' },
  ];

  readonly loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', Validators.required],
  });

  readonly rapportForm = this.fb.group({
    dateDebut: [''],
    dateFin: [''],
    campagneId: [0],
    cultureId: [0],
  });

  readonly campagneForm = this.fb.group({
    nom: ['', Validators.required],
    dateDebut: ['', Validators.required],
    dateFin: [''],
    statut: ['', Validators.required],
  });

  readonly parcelleForm = this.fb.group({
    nom: ['', Validators.required],
    superficieHa: [null as number | null, [Validators.required, Validators.min(0.01)]],
    localisation: [''],
    typeSol: [''],
    typeIrrigation: [''],
    description: [''],
  });

  readonly utilisateurForm = this.fb.group({
    nom: ['', Validators.required],
    prenom: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    motDePasse: [''],
    role: ['AGRICULTEUR' as UserRole, Validators.required],
    actif: [true, Validators.required],
  });

  readonly cultureForm = this.fb.group({
    nom: ['', Validators.required],
    variete: [''],
    surfaceHa: [null as number | null, [Validators.required, Validators.min(0.01)]],
    dateSemis: [''],
    dateRecoltePrevue: [''],
    dateRecolteReelle: [''],
    statut: ['', Validators.required],
    campagneId: [0, [Validators.required, Validators.min(1)]],
    parcelleId: [0, [Validators.required, Validators.min(1)]],
  });

  readonly operationForm = this.fb.group({
    typeOperation: ['', Validators.required],
    dateOperation: ['', Validators.required],
    description: [''],
    coutMainOeuvre: [null as number | null, [Validators.required, Validators.min(0)]],
    cultureId: [0, [Validators.required, Validators.min(1)]],
    utilisateurId: [0],
  });

  readonly recolteForm = this.fb.group({
    dateRecolte: ['', Validators.required],
    quantite: [null as number | null, [Validators.required, Validators.min(0.01)]],
    unite: ['', Validators.required],
    prixUnitaire: [null as number | null, Validators.min(0)],
    montantTotal: [null as number | null, Validators.min(0)],
    observation: [''],
    cultureId: [0, [Validators.required, Validators.min(1)]],
    utilisateurId: [0],
  });

  readonly depenseForm = this.fb.group({
    libelle: ['', Validators.required],
    montant: [null as number | null, [Validators.required, Validators.min(1)]],
    dateDepense: ['', Validators.required],
    modePaiement: [''],
    referencePiece: [''],
    cultureId: [0, [Validators.required, Validators.min(1)]],
    categorieId: [0, [Validators.required, Validators.min(1)]],
    operationAgricoleId: [0],
    utilisateurId: [0],
  });

  readonly recetteForm = this.fb.group({
    libelle: ['', Validators.required],
    montant: [null as number | null, [Validators.required, Validators.min(1)]],
    dateRecette: ['', Validators.required],
    quantite: [null as number | null, Validators.min(0.01)],
    unite: [''],
    prixUnitaire: [null as number | null, Validators.min(0.01)],
    cultureId: [0, [Validators.required, Validators.min(1)]],
    utilisateurId: [0],
  });

  readonly produitForm = this.fb.group({
    nom: ['', Validators.required],
    typeProduit: ['', Validators.required],
    unite: ['', Validators.required],
    quantiteDisponible: [null as number | null, [Validators.required, Validators.min(0)]],
    seuilAlerte: [null as number | null, [Validators.required, Validators.min(0)]],
    prixUnitaireMoyen: [null as number | null, Validators.min(0)],
  });

  readonly mouvementForm = this.fb.group({
    typeMouvement: ['', Validators.required],
    quantite: [null as number | null, [Validators.required, Validators.min(0.01)]],
    prixUnitaire: [null as number | null, Validators.min(0)],
    dateMouvement: ['', Validators.required],
    motif: [''],
    produitStockId: [0, [Validators.required, Validators.min(1)]],
    cultureId: [0],
    operationAgricoleId: [0],
    utilisateurId: [0],
  });

  constructor() {
    if (this.currentUser()) {
      this.loadAll();
    }
  }

  visibleNavItems(): { id: Section; label: string; icon: string }[] {
    return this.navItems.filter((item) => {
      if (item.id === 'utilisateurs') {
        return this.currentUser()?.role === 'ADMIN';
      }
      if (item.id === 'rapports') {
        return this.currentUser()?.role === 'ADMIN' || this.currentUser()?.role === 'COMPTABLE';
      }
      return true;
    });
  }

  login(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.loading.set(true);
    this.clearNotice();
    this.http.post<LoginResponse>(`${this.api}/auth/login`, this.loginForm.getRawValue()).subscribe({
      next: (response) => {
        this.storeAuth(response.token, response.utilisateur);
        this.authToken.set(response.token);
        this.currentUser.set(response.utilisateur);
        this.loginForm.reset({ email: '', motDePasse: '' });
        this.section.set('dashboard');
        this.mode.set('list');
        this.resetListControls();
        this.loadAll();
      },
      error: (err) => {
        this.error.set(this.readError(err));
        this.loading.set(false);
      },
    });
  }

  logout(): void {
    this.clearStoredAuth();
    this.authToken.set(null);
    this.currentUser.set(null);
    this.selected.set(null);
    this.section.set('dashboard');
    this.mode.set('list');
    this.resetListControls();
    this.clearNotice();
  }

  setSection(section: Section): void {
    this.section.set(section);
    this.mode.set('list');
    this.selected.set(null);
    this.resetListControls();
    this.clearNotice();
    if (section === 'rapports') {
      this.loadRapport();
    }
  }

  openCreate(): void {
    this.selected.set(null);
    if (this.section() === 'stocks') {
      this.stockFormKind.set('produit');
    }
    this.mode.set('create');
    this.resetCurrentForm();
  }

  openCreateStock(): void {
    this.selected.set(null);
    this.stockFormKind.set('produit');
    this.mode.set('create');
    this.resetCurrentForm();
  }

  openCreateMouvement(): void {
    this.selected.set(null);
    this.stockFormKind.set('mouvement');
    this.mode.set('create');
    this.resetCurrentForm();
  }

  openDetail(item: Entity): void {
    this.selected.set(item);
    this.mode.set('detail');
    this.clearNotice();
  }

  openEdit(item: Entity): void {
    this.selected.set(item);
    if (this.section() === 'stocks') {
      this.stockFormKind.set('produit');
    }
    this.patchCurrentForm(item);
    this.mode.set('edit');
    this.clearNotice();
  }

  backToList(): void {
    this.mode.set('list');
    this.selected.set(null);
    this.ensureValidPage();
    this.clearNotice();
  }

  loadAll(): void {
    this.loading.set(true);
    this.clearNotice();

    const requests: Promise<unknown>[] = [
      this.fetch<Dashboard>('/dashboard').then((value) => this.dashboard.set(value)),
      this.fetch<Entity[]>('/categories-depenses').then((value) => this.categories.set(value)),
      this.fetch<Entity[]>('/campagnes').then((value) => this.campagnes.set(value)),
      this.fetch<Entity[]>('/parcelles').then((value) => this.parcelles.set(value)),
      this.fetch<Entity[]>('/cultures').then((value) => this.cultures.set(value)),
      this.fetch<Entity[]>('/operations-agricoles').then((value) => this.operations.set(value)),
      this.fetch<Entity[]>('/recoltes').then((value) => this.recoltes.set(value)),
      this.fetch<Entity[]>('/depenses').then((value) => this.depenses.set(value)),
      this.fetch<Entity[]>('/recettes').then((value) => this.recettes.set(value)),
      this.fetch<ProduitStock[]>('/produits-stock').then((value) => this.produits.set(value)),
    ];

    if (this.currentUser()?.role === 'ADMIN') {
      requests.push(this.fetch<Entity[]>('/utilisateurs').then((value) => this.utilisateurs.set(value)));
    } else {
      const user = this.currentUser();
      this.utilisateurs.set(user ? [user as unknown as Entity] : []);
    }

    Promise.all(requests)
      .then(() => this.patchDefaultIds())
      .catch((err) => this.error.set(this.readError(err)))
      .finally(() => this.loading.set(false));
  }

  loadRapport(): void {
    this.loading.set(true);
    this.clearNotice();
    this.http.get<RapportSynthese>(`${this.api}/rapports/synthese${this.rapportQuery()}`, this.authOptions()).subscribe({
      next: (rapport) => {
        this.rapport.set(rapport);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(this.readError(err));
        this.loading.set(false);
      },
    });
  }

  exportRapportExcel(): void {
    this.loading.set(true);
    this.clearNotice();
    this.http.get(`${this.api}/rapports/export.csv${this.rapportQuery()}`, {
      ...this.authOptions(),
      responseType: 'blob',
    }).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'rapport-agri-compta.csv';
        link.click();
        URL.revokeObjectURL(url);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(this.readError(err));
        this.loading.set(false);
      },
    });
  }

  exportRapportPdf(): void {
    window.print();
  }

  reportCultures(): Entity[] {
    const campagneId = Number(this.rapportForm.controls.campagneId.value ?? 0);
    if (!campagneId) {
      return this.cultures();
    }
    return this.cultures().filter((culture) => this.nestedId(culture['campagne']) === campagneId);
  }

  recolteCultures(): Entity[] {
    return this.cultures().filter((culture) => {
      const statut = this.text(culture['statut']);
      return statut === 'EN_COURS' || statut === 'RECOLTEE';
    });
  }

  cultureOptionLabel(culture: Entity): string {
    return `${this.text(culture['nom'])} - ${this.text(culture['statut'])}`;
  }

  saveCampagne(): void {
    this.save('/campagnes', this.campagneForm.getRawValue(), 'Campagne enregistrée.');
  }

  saveParcelle(): void {
    this.save('/parcelles', this.parcelleForm.getRawValue(), 'Parcelle enregistrée.');
  }

  saveCulture(): void {
    const raw = this.cultureForm.getRawValue();
    const payload = { ...raw, dateRecolteReelle: raw.dateRecolteReelle || null };
    this.save('/cultures', payload, 'Culture enregistrée.');
  }

  saveOperation(): void {
    this.save('/operations-agricoles', this.cleanOptionalIds(this.operationForm.getRawValue()), 'Opération enregistrée.');
  }

  saveRecolte(): void {
    this.save('/recoltes', this.cleanOptionalIds(this.recolteForm.getRawValue()), 'Récolte enregistrée.');
  }

  saveDepense(): void {
    this.save('/depenses', this.cleanOptionalIds(this.depenseForm.getRawValue()), 'Dépense enregistrée.');
  }

  saveRecette(): void {
    this.save('/recettes', this.cleanOptionalIds(this.recetteForm.getRawValue()), 'Recette enregistrée.');
  }

  saveProduit(): void {
    this.save('/produits-stock', this.produitForm.getRawValue(), 'Produit enregistré.');
  }

  saveUtilisateur(): void {
    if (this.mode() === 'create' && !this.utilisateurForm.getRawValue().motDePasse?.trim()) {
      this.error.set('Le mot de passe initial est obligatoire.');
      return;
    }
    this.save('/utilisateurs', this.utilisateurForm.getRawValue(), 'Utilisateur enregistré.');
  }

  createMouvement(): void {
    this.loading.set(true);
    this.clearNotice();
    this.http.post(`${this.api}/mouvements-stock`, this.cleanOptionalIds(this.mouvementForm.getRawValue()), this.authOptions()).subscribe({
      next: () => {
        this.message.set('Mouvement de stock enregistré.');
        this.mode.set('list');
        this.resetListControls();
        this.loadAll();
      },
      error: (err) => {
        this.error.set(this.readError(err));
        this.loading.set(false);
      },
    });
  }

  listForCurrentSection(): Entity[] {
    switch (this.section()) {
      case 'campagnes':
        return this.campagnes();
      case 'parcelles':
        return this.parcelles();
      case 'cultures':
        return this.cultures();
      case 'operations':
        return this.operations();
      case 'recoltes':
        return this.recoltes();
      case 'depenses':
        return this.depenses();
      case 'recettes':
        return this.recettes();
      case 'stocks':
        return this.produits();
      case 'utilisateurs':
        return this.utilisateurs();
      default:
        return [];
    }
  }

  filteredListForCurrentSection(): Entity[] {
    const term = this.searchTerm().trim().toLocaleLowerCase();
    const items = this.listForCurrentSection();
    if (!term) {
      return items;
    }
    return items.filter((item) => this.searchableText(item).includes(term));
  }

  paginatedListForCurrentSection(): Entity[] {
    const currentPage = Math.min(Math.max(this.pageIndex(), 1), this.totalPages());
    const start = (currentPage - 1) * this.pageSize();
    return this.filteredListForCurrentSection().slice(start, start + this.pageSize());
  }

  totalItems(): number {
    return this.filteredListForCurrentSection().length;
  }

  totalPages(): number {
    return Math.max(1, Math.ceil(this.totalItems() / this.pageSize()));
  }

  currentPage(): number {
    return Math.min(Math.max(this.pageIndex(), 1), this.totalPages());
  }

  pageStart(): number {
    if (this.totalItems() === 0) {
      return 0;
    }
    const currentPage = this.currentPage();
    return (currentPage - 1) * this.pageSize() + 1;
  }

  pageEnd(): number {
    return Math.min(this.currentPage() * this.pageSize(), this.totalItems());
  }

  updateSearch(value: string): void {
    this.searchTerm.set(value);
    this.pageIndex.set(1);
  }

  updatePageSize(value: string): void {
    this.pageSize.set(Number(value) || 10);
    this.pageIndex.set(1);
  }

  previousPage(): void {
    this.pageIndex.set(Math.max(1, this.pageIndex() - 1));
  }

  nextPage(): void {
    this.pageIndex.set(Math.min(this.totalPages(), this.pageIndex() + 1));
  }

  listColumns(): ListColumn[] {
    switch (this.section()) {
      case 'campagnes':
        return [
          { label: 'Nom', value: (item) => this.text(item['nom']) },
          { label: 'Date début', value: (item) => this.text(item['dateDebut']) },
          { label: 'Date fin', value: (item) => this.text(item['dateFin']) },
          { label: 'Statut', value: (item) => this.text(item['statut']) },
        ];
      case 'parcelles':
        return [
          { label: 'Nom', value: (item) => this.text(item['nom']) },
          { label: 'Superficie', value: (item) => `${this.text(item['superficieHa'])} ha` },
          { label: 'Localisation', value: (item) => this.text(item['localisation']) },
          { label: 'Type de sol', value: (item) => this.text(item['typeSol']) },
          { label: 'Irrigation', value: (item) => this.text(item['typeIrrigation']) },
        ];
      case 'cultures':
        return [
          { label: 'Nom', value: (item) => this.text(item['nom']) },
          { label: 'Variété', value: (item) => this.text(item['variete']) },
          { label: 'Surface', value: (item) => `${this.text(item['surfaceHa'])} ha` },
          { label: 'Statut', value: (item) => this.text(item['statut']) },
        ];
      case 'operations':
        return [
          { label: 'Type', value: (item) => this.text(item['typeOperation']) },
          { label: 'Date', value: (item) => this.text(item['dateOperation']) },
          { label: 'Culture', value: (item) => this.nestedName(item['culture']) },
          { label: 'Coût', value: (item) => this.formatAmount(Number(item['coutMainOeuvre'] ?? 0)) },
        ];
      case 'recoltes':
        return [
          { label: 'Culture', value: (item) => this.nestedName(item['culture']) },
          { label: 'Date récolte', value: (item) => this.text(item['dateRecolte']) },
          { label: 'Quantité', value: (item) => `${this.text(item['quantite'])} ${this.text(item['unite'])}` },
          { label: 'Montant', value: (item) => this.formatAmount(Number(item['montantTotal'] ?? 0)) },
        ];
      case 'depenses':
        return [
          { label: 'Libellé', value: (item) => this.text(item['libelle']) },
          { label: 'Date', value: (item) => this.text(item['dateDepense']) },
          { label: 'Culture', value: (item) => this.nestedName(item['culture']) },
          { label: 'Montant', value: (item) => this.formatAmount(Number(item['montant'] ?? 0)) },
        ];
      case 'recettes':
        return [
          { label: 'Libellé', value: (item) => this.text(item['libelle']) },
          { label: 'Date', value: (item) => this.text(item['dateRecette']) },
          { label: 'Culture', value: (item) => this.nestedName(item['culture']) },
          { label: 'Montant', value: (item) => this.formatAmount(Number(item['montant'] ?? 0)) },
        ];
      case 'stocks':
        return [
          { label: 'Produit', value: (item) => this.text(item['nom']) },
          { label: 'Type', value: (item) => this.text(item['typeProduit']) },
          { label: 'Quantité', value: (item) => `${this.text(item['quantiteDisponible'])} ${this.text(item['unite'])}` },
          { label: 'Seuil', value: (item) => this.text(item['seuilAlerte']) },
        ];
      case 'utilisateurs':
        return [
          { label: 'Nom', value: (item) => `${this.text(item['prenom'])} ${this.text(item['nom'])}` },
          { label: 'Email', value: (item) => this.text(item['email']) },
          { label: 'Rôle', value: (item) => this.text(item['role']) },
          { label: 'Statut', value: (item) => item['actif'] === false ? 'Inactif' : 'Actif' },
        ];
      default:
        return [];
    }
  }

  detailRows(item: Entity | null): { label: string; value: string }[] {
    if (!item) {
      return [];
    }

    switch (this.section()) {
      case 'campagnes':
        return this.listColumns().map((column) => ({ label: column.label, value: column.value(item) }));
      case 'parcelles':
        return [
          ...this.listColumns().map((column) => ({ label: column.label, value: column.value(item) })),
          { label: 'Description', value: this.text(item['description']) },
        ];
      case 'cultures':
        return [
          ...this.listColumns().map((column) => ({ label: column.label, value: column.value(item) })),
          { label: 'Campagne', value: this.nestedName(item['campagne']) },
          { label: 'Parcelle', value: this.nestedName(item['parcelle']) },
          { label: 'Date semis', value: this.text(item['dateSemis']) },
          { label: 'Récolte prévue', value: this.text(item['dateRecoltePrevue']) },
          { label: 'Récolte réelle', value: this.text(item['dateRecolteReelle']) },
        ];
      case 'operations':
        return [
          ...this.listColumns().map((column) => ({ label: column.label, value: column.value(item) })),
          { label: 'Description', value: this.text(item['description']) },
        ];
      case 'recoltes':
        return [
          ...this.listColumns().map((column) => ({ label: column.label, value: column.value(item) })),
          { label: 'Prix unitaire', value: this.formatAmount(Number(item['prixUnitaire'] ?? 0)) },
          { label: 'Observation', value: this.text(item['observation']) },
        ];
      case 'depenses':
        return [
          ...this.listColumns().map((column) => ({ label: column.label, value: column.value(item) })),
          { label: 'Catégorie', value: this.nestedName(item['categorie']) },
          { label: 'Référence', value: this.text(item['referencePiece']) },
        ];
      case 'recettes':
        return [
          ...this.listColumns().map((column) => ({ label: column.label, value: column.value(item) })),
          { label: 'Quantité', value: `${this.text(item['quantite'])} ${this.text(item['unite'])}` },
          { label: 'Prix unitaire', value: this.formatAmount(Number(item['prixUnitaire'] ?? 0)) },
        ];
      case 'stocks':
        return [
          ...this.listColumns().map((column) => ({ label: column.label, value: column.value(item) })),
          { label: 'Prix moyen', value: this.formatAmount(Number(item['prixUnitaireMoyen'] ?? 0)) },
        ];
      case 'utilisateurs':
        return this.listColumns().map((column) => ({ label: column.label, value: column.value(item) }));
      default:
        return [];
    }
  }

  itemTitle(item: Entity): string {
    if (this.section() === 'operations') {
      return this.text(item['typeOperation']);
    }
    if (this.section() === 'recoltes') {
      return `${this.nestedName(item['culture'])} - ${this.text(item['dateRecolte'])}`;
    }
    if (this.section() === 'utilisateurs') {
      return `${this.text(item['prenom'])} ${this.text(item['nom'])}`;
    }
    return this.text(item['nom'] ?? item['libelle'] ?? `Élément ${item.id}`);
  }

  sectionTitle(): string {
    return this.navItems.find((item) => item.id === this.section())?.label ?? '';
  }

  iconPath(icon: string): string {
    return `icons/${icon}.svg`;
  }

  formatAmount(value: number | null | undefined): string {
    return new Intl.NumberFormat('fr-SN', {
      style: 'currency',
      currency: 'XOF',
      maximumFractionDigits: 0,
    }).format(value ?? 0);
  }

  formatAbsoluteAmount(value: number | null | undefined): string {
    return this.formatAmount(Math.abs(value ?? 0));
  }

  resultLabel(value: number | null | undefined): string {
    return (value ?? 0) < 0 ? 'Perte' : 'Bénéfice';
  }

  private save(path: string, payload: unknown, successMessage: string): void {
    const current = this.selected();
    const request = this.mode() === 'edit' && current
      ? this.http.put(`${this.api}${path}/${current.id}`, payload, this.authOptions())
      : this.http.post(`${this.api}${path}`, payload, this.authOptions());

    this.loading.set(true);
    this.clearNotice();
    request.subscribe({
      next: () => {
        this.message.set(successMessage);
        this.mode.set('list');
        this.selected.set(null);
        this.resetListControls();
        this.loadAll();
      },
      error: (err) => {
        this.error.set(this.readError(err));
        this.loading.set(false);
      },
    });
  }

  private fetch<T>(path: string): Promise<T> {
    return new Promise<T>((resolve, reject) => {
      this.http.get<T>(`${this.api}${path}`, this.authOptions()).subscribe({ next: resolve, error: reject });
    });
  }

  private authOptions(): { headers: Record<string, string> } {
    const token = this.authToken();
    return token ? { headers: { Authorization: `Bearer ${token}` } } : { headers: {} };
  }

  private resetListControls(): void {
    this.searchTerm.set('');
    this.pageIndex.set(1);
  }

  private ensureValidPage(): void {
    const lastPage = this.totalPages();
    if (this.pageIndex() > lastPage) {
      this.pageIndex.set(lastPage);
    }
    if (this.pageIndex() < 1) {
      this.pageIndex.set(1);
    }
  }

  private searchableText(item: Entity): string {
    const columnText = this.listColumns().map((column) => column.value(item));
    return [
      this.itemTitle(item),
      ...columnText,
      ...this.flattenSearchValues(item),
    ].join(' ').toLocaleLowerCase();
  }

  private flattenSearchValues(value: unknown): string[] {
    if (value === null || value === undefined) {
      return [];
    }
    if (typeof value !== 'object') {
      return [String(value)];
    }
    if (Array.isArray(value)) {
      return value.flatMap((item) => this.flattenSearchValues(item));
    }
    return Object.values(value as Record<string, unknown>).flatMap((item) => this.flattenSearchValues(item));
  }

  private rapportQuery(): string {
    const raw = this.rapportForm.getRawValue();
    const params = new URLSearchParams();
    if (raw.dateDebut) params.set('dateDebut', raw.dateDebut);
    if (raw.dateFin) params.set('dateFin', raw.dateFin);
    if (raw.campagneId) params.set('campagneId', String(raw.campagneId));
    if (raw.cultureId) params.set('cultureId', String(raw.cultureId));
    const query = params.toString();
    return query ? `?${query}` : '';
  }

  private readStoredUser(): AuthUser | null {
    if (!this.readStoredToken()) {
      return null;
    }

    const value = sessionStorage.getItem('agri-compta-user');
    if (!value) {
      return null;
    }
    try {
      return JSON.parse(value) as AuthUser;
    } catch {
      this.clearStoredAuth();
      return null;
    }
  }

  private storeAuth(token: string, utilisateur: AuthUser): void {
    sessionStorage.setItem('agri-compta-token', token);
    sessionStorage.setItem('agri-compta-user', JSON.stringify(utilisateur));
    localStorage.removeItem('agri-compta-token');
    localStorage.removeItem('agri-compta-user');
  }

  private readStoredToken(): string | null {
    localStorage.removeItem('agri-compta-token');
    localStorage.removeItem('agri-compta-user');

    const token = sessionStorage.getItem('agri-compta-token');
    if (!token || this.isTokenExpired(token)) {
      this.clearStoredAuth();
      return null;
    }
    return token;
  }

  private clearStoredAuth(): void {
    sessionStorage.removeItem('agri-compta-token');
    sessionStorage.removeItem('agri-compta-user');
    localStorage.removeItem('agri-compta-token');
    localStorage.removeItem('agri-compta-user');
  }

  private isTokenExpired(token: string): boolean {
    const payload = this.decodeToken(token);
    return !payload || payload.expiresAt <= Math.floor(Date.now() / 1000);
  }

  private decodeToken(token: string): TokenPayload | null {
    const encodedPayload = token.split('.')[0];
    if (!encodedPayload) {
      return null;
    }

    try {
      const payload = atob(encodedPayload.replace(/-/g, '+').replace(/_/g, '/'));
      const [utilisateurId, role, email, expiresAt] = payload.split('|');
      return {
        utilisateurId: Number(utilisateurId),
        role: role as UserRole,
        email,
        expiresAt: Number(expiresAt),
      };
    } catch {
      return null;
    }
  }

  private resetCurrentForm(): void {
    switch (this.section()) {
      case 'campagnes':
        this.campagneForm.reset({ nom: '', dateDebut: '', dateFin: '', statut: '' });
        break;
      case 'parcelles':
        this.parcelleForm.reset({
          nom: '',
          superficieHa: null,
          localisation: '',
          typeSol: '',
          typeIrrigation: '',
          description: '',
        });
        break;
      case 'cultures':
        this.cultureForm.reset({
          nom: '',
          variete: '',
          surfaceHa: null,
          dateSemis: '',
          dateRecoltePrevue: '',
          dateRecolteReelle: '',
          statut: '',
          campagneId: 0,
          parcelleId: 0,
        });
        break;
      case 'operations':
        this.operationForm.reset({
          typeOperation: '',
          dateOperation: '',
          description: '',
          coutMainOeuvre: null,
          cultureId: 0,
          utilisateurId: 0,
        });
        break;
      case 'recoltes':
        this.recolteForm.reset({
          dateRecolte: '',
          quantite: null,
          unite: '',
          prixUnitaire: null,
          montantTotal: null,
          observation: '',
          cultureId: 0,
          utilisateurId: 0,
        });
        break;
      case 'depenses':
        this.depenseForm.reset({
          libelle: '',
          montant: null,
          dateDepense: '',
          modePaiement: '',
          referencePiece: '',
          cultureId: 0,
          categorieId: 0,
          operationAgricoleId: 0,
          utilisateurId: 0,
        });
        break;
      case 'recettes':
        this.recetteForm.reset({
          libelle: '',
          montant: null,
          dateRecette: '',
          quantite: null,
          unite: '',
          prixUnitaire: null,
          cultureId: 0,
          utilisateurId: 0,
        });
        break;
      case 'stocks':
        this.produitForm.reset({
          nom: '',
          typeProduit: '',
          unite: '',
          quantiteDisponible: null,
          seuilAlerte: null,
          prixUnitaireMoyen: null,
        });
        this.mouvementForm.reset({
          typeMouvement: '',
          quantite: null,
          prixUnitaire: null,
          dateMouvement: '',
          motif: '',
          produitStockId: 0,
          cultureId: 0,
          operationAgricoleId: 0,
          utilisateurId: 0,
        });
        break;
      case 'utilisateurs':
        this.utilisateurForm.reset({
          nom: '',
          prenom: '',
          email: '',
          motDePasse: '',
          role: 'AGRICULTEUR',
          actif: true,
        });
        break;
    }
    this.patchDefaultIds();
  }

  private patchCurrentForm(item: Entity): void {
    switch (this.section()) {
      case 'campagnes':
        this.campagneForm.patchValue({
          nom: this.text(item['nom']),
          dateDebut: this.formText(item['dateDebut']),
          dateFin: this.formText(item['dateFin']),
          statut: this.text(item['statut']) || 'PLANIFIEE',
        });
        break;
      case 'parcelles':
        this.parcelleForm.patchValue({
          nom: this.text(item['nom']),
          superficieHa: Number(item['superficieHa'] ?? 0),
          localisation: this.formText(item['localisation']),
          typeSol: this.formText(item['typeSol']),
          typeIrrigation: this.formText(item['typeIrrigation']),
          description: this.formText(item['description']),
        });
        break;
      case 'cultures':
        this.cultureForm.patchValue({
          nom: this.text(item['nom']),
          variete: this.formText(item['variete']),
          surfaceHa: Number(item['surfaceHa'] ?? 0),
          dateSemis: this.formText(item['dateSemis']),
          dateRecoltePrevue: this.formText(item['dateRecoltePrevue']),
          dateRecolteReelle: this.formText(item['dateRecolteReelle']),
          statut: this.text(item['statut']) || 'PLANIFIEE',
          campagneId: this.nestedId(item['campagne']),
          parcelleId: this.nestedId(item['parcelle']),
        });
        break;
      case 'operations':
        this.operationForm.patchValue({
          typeOperation: this.text(item['typeOperation']) || 'AUTRE',
          dateOperation: this.formText(item['dateOperation']),
          description: this.formText(item['description']),
          coutMainOeuvre: Number(item['coutMainOeuvre'] ?? 0),
          cultureId: this.nestedId(item['culture']),
          utilisateurId: this.nestedId(item['utilisateur']),
        });
        break;
      case 'recoltes':
        this.recolteForm.patchValue({
          dateRecolte: this.formText(item['dateRecolte']),
          quantite: Number(item['quantite'] ?? 0),
          unite: this.formText(item['unite']),
          prixUnitaire: Number(item['prixUnitaire'] ?? 0),
          montantTotal: Number(item['montantTotal'] ?? 0),
          observation: this.formText(item['observation']),
          cultureId: this.nestedId(item['culture']),
          utilisateurId: this.nestedId(item['utilisateur']),
        });
        break;
      case 'depenses':
        this.depenseForm.patchValue({
          libelle: this.text(item['libelle']),
          montant: Number(item['montant'] ?? 0),
          dateDepense: this.formText(item['dateDepense']),
          modePaiement: this.formText(item['modePaiement']),
          referencePiece: this.formText(item['referencePiece']),
          cultureId: this.nestedId(item['culture']),
          categorieId: this.nestedId(item['categorie']),
          operationAgricoleId: this.nestedId(item['operationAgricole']),
          utilisateurId: this.nestedId(item['utilisateur']),
        });
        break;
      case 'recettes':
        this.recetteForm.patchValue({
          libelle: this.text(item['libelle']),
          montant: Number(item['montant'] ?? 0),
          dateRecette: this.formText(item['dateRecette']),
          quantite: Number(item['quantite'] ?? 0),
          unite: this.formText(item['unite']),
          prixUnitaire: Number(item['prixUnitaire'] ?? 0),
          cultureId: this.nestedId(item['culture']),
          utilisateurId: this.nestedId(item['utilisateur']),
        });
        break;
      case 'stocks':
        this.produitForm.patchValue({
          nom: this.text(item['nom']),
          typeProduit: this.text(item['typeProduit']) || 'AUTRE',
          unite: this.formText(item['unite']),
          quantiteDisponible: Number(item['quantiteDisponible'] ?? 0),
          seuilAlerte: Number(item['seuilAlerte'] ?? 0),
          prixUnitaireMoyen: Number(item['prixUnitaireMoyen'] ?? 0),
        });
        break;
      case 'utilisateurs':
        this.utilisateurForm.patchValue({
          nom: this.text(item['nom']),
          prenom: this.formText(item['prenom']),
          email: this.formText(item['email']),
          motDePasse: '',
          role: (this.text(item['role']) || 'AGRICULTEUR') as UserRole,
          actif: item['actif'] !== false,
        });
        break;
    }
  }

  private patchDefaultIds(): void {
    const utilisateurId = this.currentUser()?.id ?? this.utilisateurs()[0]?.id ?? 0;

    this.operationForm.patchValue({ utilisateurId });
    this.recolteForm.patchValue({ utilisateurId });
    this.depenseForm.patchValue({ utilisateurId });
    this.recetteForm.patchValue({ utilisateurId });
    this.mouvementForm.patchValue({ utilisateurId });
  }

  private cleanOptionalIds<T extends Record<string, unknown>>(payload: T): T {
    return Object.fromEntries(
      Object.entries(payload).map(([key, value]) => [key, key.endsWith('Id') && value === 0 ? null : value]),
    ) as T;
  }

  private nestedName(value: unknown): string {
    if (!value || typeof value !== 'object') {
      return '-';
    }
    const entity = value as Entity;
    return this.text(entity['nom'] ?? entity['libelle'] ?? `#${entity.id}`);
  }

  private nestedId(value: unknown): number {
    if (!value || typeof value !== 'object') {
      return 0;
    }
    return Number((value as Entity).id ?? 0);
  }

  private text(value: unknown): string {
    if (value === null || value === undefined || value === '') {
      return '-';
    }
    return String(value);
  }

  private formText(value: unknown): string {
    if (value === null || value === undefined || value === '') {
      return '';
    }
    return String(value);
  }

  private readError(err: unknown): string {
    const value = err as { status?: number; error?: { message?: string; errors?: Record<string, string> }; message?: string };
    if (value.status === 401) {
      this.logout();
      return 'Session expiree. Veuillez vous reconnecter.';
    }
    if (value.error?.errors && Object.keys(value.error.errors).length) {
      return Object.values(value.error.errors).join(' ');
    }
    return value.error?.message ?? value.message ?? 'Une erreur est survenue.';
  }

  private clearNotice(): void {
    this.message.set('');
    this.error.set('');
  }
}
