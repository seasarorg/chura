package ${configs.rootpackagename}.${configs.subapplicationrootpackagename};


public abstract class AbstractCrud${configs.pagesuffix} {

	private int crudType = 0;

	public AbstractCrud${configs.pagesuffix}() {
	}
	
	public int getCrudType() {
		return this.crudType;
	}
	
	public void setCrudType(int type) {
		this.crudType = type;
	}

	public boolean isCreate() {
		return getCrudType() == CrudType.CREATE;
	}
	
	public boolean isRead() {
		return getCrudType() == CrudType.READ;
	}

	public boolean isUpdate() {
		return getCrudType() == CrudType.UPDATE;
	}

	public boolean isDelete() {
		return getCrudType() == CrudType.DELETE;
	}

}