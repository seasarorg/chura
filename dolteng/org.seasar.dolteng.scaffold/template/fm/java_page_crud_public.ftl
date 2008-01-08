package ${configs.rootpackagename}.${configs.subapplicationrootpackagename};


public abstract class AbstractCrud${configs.pagesuffix} {

	public int crudType = 0;

	public AbstractCrud${configs.pagesuffix}() {
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