package com.softwarearchetypes.pricing.formula.infrastructure;

import com.softwarearchetypes.pricing.formula.domain.FormulaPricingEntity;
import com.softwarearchetypes.pricing.formula.domain.FormulaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

interface JpaFormulaRepository extends JpaRepository<FormulaPricingJpaEntity, UUID> {
}

@Repository
class JpaBasedFormulaRepository implements FormulaRepository {

    private final JpaFormulaRepository repository;

    JpaBasedFormulaRepository(JpaFormulaRepository repository) {
        this.repository = repository;
    }

    @Override
    public UUID save(FormulaPricingEntity formulaPricingEntity) {

        var entity = new FormulaPricingJpaEntity(
                formulaPricingEntity.creationDate(),
                formulaPricingEntity.name(),
                formulaPricingEntity.formula(),
                formulaPricingEntity.inputDataClass(),
                formulaPricingEntity.inputDataJson()
        );

        return repository.save(entity).getId();
    }

    @Override
    public Optional<FormulaPricingEntity> findById(UUID id) {
        return repository.findById(id)
                .map(entity -> new FormulaPricingEntity(
                        entity.getName(),
                        entity.getFormula(),
                        entity.getInputDataClass(),
                        entity.getInputDataJson(),
                        entity.getCreationDate()
                ));
    }
}
