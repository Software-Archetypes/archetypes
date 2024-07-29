package com.softwarearchetypes.pricing.formula;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface FormulaJpaRepository extends JpaRepository<FormulaPricingEntity, UUID> {
}
