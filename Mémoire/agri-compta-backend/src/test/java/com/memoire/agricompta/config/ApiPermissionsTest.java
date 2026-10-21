package com.memoire.agricompta.config;

import static org.junit.jupiter.api.Assertions.*;
import com.memoire.agricompta.domain.enums.Role;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApiPermissionsTest {
    private final ApiPermissions policy = new ApiPermissions();

    @Test
    void everyRoleCanReadProductionFinancesAndReports() {
        for (Role role : Role.values()) {
            for (String path : List.of("campagnes", "parcelles", "cultures", "recoltes", "operations-agricoles",
                    "produits-stock", "mouvements-stock", "depenses", "recettes", "exploitations", "categories-depenses")) {
                assertTrue(policy.allows(role, "GET", "/api/" + path), role + " " + path);
                assertTrue(policy.allows(role, "GET", "/api/" + path + "/1"));
                assertTrue(policy.allows(role, "HEAD", "/api/" + path));
            }
            assertTrue(policy.allows(role, "GET", "/api/dashboard"));
            assertTrue(policy.allows(role, "GET", "/api/produits-stock/alertes"));
            assertTrue(policy.allows(role, "GET", "/api/rapports/synthese"));
            assertTrue(policy.allows(role, "GET", "/api/rapports/export.csv"));
            assertTrue(policy.allows(role, "GET", "/api/depenses/1/historique"));
        }
    }

    @Test
    void onlyFarmerCanWriteProductionAndStock() {
        for (Role role : Role.values()) {
            for (String path : List.of("campagnes", "parcelles", "cultures", "operations-agricoles", "recoltes", "produits-stock")) {
                assertEquals(role == Role.AGRICULTEUR, policy.allows(role, "POST", "/api/" + path));
                assertEquals(role == Role.AGRICULTEUR, policy.allows(role, "PUT", "/api/" + path + "/1"));
                assertFalse(policy.allows(role, "DELETE", "/api/" + path + "/1"));
            }
            assertEquals(role == Role.AGRICULTEUR, policy.allows(role, "POST", "/api/mouvements-stock"));
            assertFalse(policy.allows(role, "PUT", "/api/mouvements-stock/1"));
            assertFalse(policy.allows(role, "DELETE", "/api/mouvements-stock/1"));
        }
    }

    @Test
    void farmerCanRecordMoneyButOnlyAccountantCanCorrectOrCancel() {
        for (Role role : Role.values()) {
            for (String resource : List.of("depenses", "recettes")) {
                assertEquals(role != Role.ADMIN, policy.allows(role, "POST", "/api/" + resource));
                assertEquals(role == Role.COMPTABLE, policy.allows(role, "PUT", "/api/" + resource + "/1"));
                assertEquals(role == Role.COMPTABLE, policy.allows(role, "POST", "/api/" + resource + "/1/annulation"));
                assertFalse(policy.allows(role, "DELETE", "/api/" + resource + "/1"));
            }
        }
    }

    @Test
    void administrationIsRestrictedAndUnknownRoutesAreDenied() {
        for (Role role : Role.values()) {
            assertEquals(role == Role.ADMIN, policy.allows(role, "GET", "/api/utilisateurs"));
            assertEquals(role == Role.ADMIN, policy.allows(role, "POST", "/api/exploitations"));
            assertEquals(role == Role.ADMIN, policy.allows(role, "PUT", "/api/utilisateurs/1"));
            assertFalse(policy.allows(role, "POST", "/api/unknown"));
            assertFalse(policy.allows(role, "GET", "/api/utilisateurs-extra"));
            assertFalse(policy.allows(role, "PATCH", "/api/recettes/1"));
            assertFalse(policy.allows(role, "POST", "/api/depenses/1/historique"));
        }
        assertFalse(policy.allows(null, "GET", "/api/dashboard"));
    }
}
