package Cheqideh.service.crud;

public interface CrudService<T, ID> {
    void add(T entity);
    void removeById(ID id);
    T findById(ID id);
}