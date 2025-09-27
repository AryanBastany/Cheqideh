package cheqideh.service.crud;

public interface CrudService<T, ID> {
    T add(T entity);
    void removeById(ID id);
    T findById(ID id);
}