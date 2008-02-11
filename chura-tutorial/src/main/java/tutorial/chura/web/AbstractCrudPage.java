package tutorial.chura.web;


public abstract class AbstractCrudPage {

	public int crudType = 0;

	public AbstractCrudPage() {
	}
	
	public boolean isCreate() {
		return crudType == CrudType.CREATE;
	}
	
	public boolean isRead() {
		return crudType == CrudType.READ;
	}

	public boolean isUpdate() {
		return crudType == CrudType.UPDATE;
	}

	public boolean isDelete() {
		return crudType == CrudType.DELETE;
	}

}