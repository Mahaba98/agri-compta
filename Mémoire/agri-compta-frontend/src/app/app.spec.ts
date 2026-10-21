import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { App } from './app';

describe('Permissions des vues', () => {
  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  it('masque les commandes agricoles au comptable', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    app.currentUser.set({ id: 1, nom: 'Test', prenom: 'Test', email: 'test@example.test', role: 'COMPTABLE' });
    app.section.set('parcelles');
    app.parcelles.set([{ id: 1, nom: 'Parcelle' }]);
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('[aria-label="Consulter"]')).not.toBeNull();
    expect(element.querySelector('[aria-label="Modifier"]')).toBeNull();
    expect([...element.querySelectorAll('button')].some((button) => button.textContent?.trim() === 'Créer')).toBe(false);
  });

  it('autorise les rapports et la saisie au fermier sans correction financière', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    app.currentUser.set({ id: 1, nom: 'Test', prenom: 'Test', email: 'test@example.test', role: 'AGRICULTEUR' });
    expect(app.visibleNavItems().some((item) => item.id === 'rapports')).toBe(true);
    expect(app.visibleNavItems().some((item) => item.id === 'utilisateurs')).toBe(false);
    app.section.set('depenses');
    expect(app.canCreate()).toBe(true);
    expect(app.canEdit({ id: 1 })).toBe(false);
    app.openEdit({ id: 1 });
    expect(app.mode()).toBe('list');
  });

  it('interdit la modification des écritures annulées au comptable', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    app.currentUser.set({ id: 1, nom: 'Test', prenom: 'Test', email: 'test@example.test', role: 'COMPTABLE' });
    app.section.set('recettes');
    app.selected.set({ id: 1, annule: true });
    expect(app.canEdit()).toBe(false);
    expect(app.canCancel()).toBe(false);
  });

  it('réserve les changements administratifs à ADMIN', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    app.currentUser.set({ id: 1, nom: 'Test', prenom: 'Test', email: 'test@example.test', role: 'ADMIN' });
    app.section.set('exploitations');
    expect(app.canCreate()).toBe(true);
    app.section.set('cultures');
    expect(app.canCreate()).toBe(false);
    expect(app.canEdit({ id: 1 })).toBe(false);
  });
});
