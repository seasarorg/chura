select e.*, d.dept_name
from emp e left outer join dept d on e.dept_id = d.id
/*BEGIN*/
where
    /*IF minSal != null*/
        sal >= /*minSal*/1000
    /*END*/
    /*IF maxSal != null*/
        and sal <= /*maxSal*/2000
    /*END*/
/*END*/