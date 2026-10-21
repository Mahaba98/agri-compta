package com.memoire.agricompta.config;

import com.memoire.agricompta.domain.enums.Role;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ApiPermissions {
    private static final Set<String> AGRICULTURE = Set.of(
            "campagnes", "parcelles", "cultures", "operations-agricoles", "recoltes", "produits-stock");
    private static final Set<String> FINANCES = Set.of("depenses", "recettes");

    public boolean allows(Role role, String method, String path) {
        if (role == null || !path.startsWith("/api/")) {
            return false;
        }
        String resource = path.substring(5).split("/", 2)[0];
        String suffix = path.substring(5 + resource.length());
        boolean read = method.equals("GET") || method.equals("HEAD");
        boolean collection = suffix.isEmpty() || suffix.equals("/");
        boolean item = suffix.matches("/[0-9]+");

        if (resource.equals("utilisateurs")) {
            return role == Role.ADMIN && ((read && (collection || item))
                    || (method.equals("POST") && collection)
                    || ((method.equals("PUT") || method.equals("DELETE")) && item));
        }
        if (resource.equals("exploitations")) {
            return (read && (collection || item)) || (role == Role.ADMIN
                    && ((method.equals("POST") && collection)
                    || ((method.equals("PUT") || method.equals("DELETE")) && item)));
        }
        if (resource.equals("dashboard")) {
            return read && collection;
        }
        if (resource.equals("rapports")) {
            return read && (suffix.equals("/synthese") || suffix.equals("/export.csv"));
        }
        if (resource.equals("categories-depenses")) {
            return (read && (collection || item)) || ((role == Role.ADMIN || role == Role.COMPTABLE)
                    && ((method.equals("POST") && collection) || (method.equals("PUT") && item)));
        }
        if (AGRICULTURE.contains(resource)) {
            return (read && (collection || item || (resource.equals("produits-stock") && suffix.equals("/alertes"))))
                    || (role == Role.AGRICULTEUR && ((method.equals("POST") && collection)
                    || (method.equals("PUT") && item)));
        }
        if (resource.equals("mouvements-stock")) {
            return (read && (collection || item))
                    || (role == Role.AGRICULTEUR && method.equals("POST") && collection);
        }
        if (FINANCES.contains(resource)) {
            return (read && (collection || item || suffix.matches("/[0-9]+/historique")))
                    || ((role == Role.AGRICULTEUR || role == Role.COMPTABLE) && method.equals("POST") && collection)
                    || (role == Role.COMPTABLE && ((method.equals("PUT") && item)
                    || (method.equals("POST") && suffix.matches("/[0-9]+/annulation"))));
        }
        return false;
    }
}
