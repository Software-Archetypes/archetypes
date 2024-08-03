package com.softwarearchetypes.pricing.shared;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.StreamSupport;


public abstract class AbstractInMemoryRepository<T extends AbstractBaseEntity> implements JpaRepository<T, UUID> {

    protected final Map<UUID, T> collection;

    public AbstractInMemoryRepository() {
        collection = new ConcurrentHashMap<>();
    }

    @Override
    public void flush() {
        //Just do nothing, no need to flush
    }

    @Override
    public <S extends T> @NotNull S saveAndFlush(@NotNull S entity) {
        return save(entity);
    }

    @Override
    public <S extends T> @NotNull List<S> saveAllAndFlush(@NotNull Iterable<S> entities) {
        return saveAll(entities);
    }

    @Override
    public void deleteAllInBatch(@NotNull Iterable<T> entities) {
        this.deleteAll(entities);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<UUID> uuids) {
        uuids.forEach(this::deleteById);
    }

    @Override
    public void deleteAllInBatch() {
        throw new OperationNotSupportedInRepository("Implement this if needed");
    }

    @Override
    public @NotNull T getOne(@NotNull UUID uuid) {
        return getById(uuid);
    }

    @Override
    public T getById(@NotNull UUID uuid) {
        return collection.getOrDefault(uuid, null);
    }

    @Override
    public @NotNull T getReferenceById(@NotNull UUID uuid) {
        throw new OperationNotSupportedInRepository("Implement this if needed");
    }

    @Override
    public <S extends T> @NotNull List<S> findAll(@NotNull Example<S> example) {
        throw new OperationNotSupportedInRepository("Implement this if needed");
    }

    @Override
    public <S extends T> @NotNull List<S> findAll(@NotNull Example<S> example, @NotNull Sort sort) {
        throw new OperationNotSupportedInRepository("Implement this if needed");
    }

    @Override
    public <S extends T> @NotNull List<S> saveAll(Iterable<S> entities) {
        return StreamSupport.stream(entities.spliterator(), false)
                .map(this::save)
                .toList();
    }

    @Override
    public @NotNull List<T> findAll() {
        return collection.values().stream().toList();
    }

    @Override
    public @NotNull List<T> findAllById(@NotNull Iterable<UUID> uuids) {
        throw new OperationNotSupportedInRepository("Implement this if needed");
    }

    @Override
    public <S extends T> @NotNull S save(@NotNull S entity) {
        setId(entity);
        collection.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public @NotNull Optional<T> findById(@NotNull UUID uuid) {
        return Optional.ofNullable(this.getById(uuid));
    }

    @Override
    public boolean existsById(@NotNull UUID uuid) {
        return findById(uuid).isPresent();
    }

    @Override
    public long count() {
        return collection.size();
    }

    @Override
    public void deleteById(@NotNull UUID uuid) {
        collection.remove(uuid);
    }

    @Override
    public void delete(T entity) {
        deleteById(entity.getId());
    }

    @Override
    public void deleteAllById(Iterable<? extends UUID> uuids) {
        throw new OperationNotSupportedInRepository("Implement this if needed");

    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        throw new OperationNotSupportedInRepository("Implement this if needed");

    }

    @Override
    public void deleteAll() {
        throw new OperationNotSupportedInRepository("Implement this if needed");

    }

    @Override
    public List<T> findAll(Sort sort) {
        throw new OperationNotSupportedInRepository("Implement this if needed");

    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        throw new OperationNotSupportedInRepository("Implement this if needed");

    }

    @Override
    public <S extends T> Optional<S> findOne(Example<S> example) {
        throw new OperationNotSupportedInRepository("Implement this if needed");

    }

    @Override
    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new OperationNotSupportedInRepository("Implement this if needed");

    }

    @Override
    public <S extends T> long count(Example<S> example) {
        throw new OperationNotSupportedInRepository("Implement this if needed");

    }

    @Override
    public <S extends T> boolean exists(Example<S> example) {
        throw new OperationNotSupportedInRepository("Implement this if needed");

    }

    @Override
    public <S extends T, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new OperationNotSupportedInRepository("Implement this if needed");
    }


    private void setId(T object) {
        var idFieldName = "id";
        Field field = getIdField(object, idFieldName);
        // walked to the top of class hierarchy without finding field
        if (field == null) {
            throw new RuntimeException("No such field like: " + idFieldName);
        } else {
            try {
                field.setAccessible(true);
                field.set(object, UUID.randomUUID());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Nullable
    private <E extends AbstractBaseEntity> Field getIdField(E object, String idFieldName) {
        Class<?> currentClass = object.getClass();
        Field field = null;
        //We need to iterate throught class hierarchy to find base class that stores id
        //stop when we got field or reached top of class hierarchy
        while (field == null && currentClass != null) {
            try {
                field = currentClass.getDeclaredField(idFieldName);
            } catch (NoSuchFieldException e) {
                // only get super-class when we couldn't find field
                currentClass = currentClass.getSuperclass();
            }
        }
        return field;
    }
}
