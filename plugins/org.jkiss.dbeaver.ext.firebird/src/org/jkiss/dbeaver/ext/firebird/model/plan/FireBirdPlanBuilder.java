package org.jkiss.dbeaver.ext.firebird.model.plan;

import java.util.ArrayList;
import java.util.List;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;

public class FireBirdPlanBuilder {
	
	private String plan;
	
	public FireBirdPlanBuilder(String plan) {
		super();
		this.plan = plan;
	}

	public List<FireBirdPlanNode> Build(JDBCSession session) throws DBCException {
		List<FireBirdPlanNode> rootNodes = new ArrayList<>();
		String [] plans = plan.split("\\n");
		for (String plan: plans) {
			FireBirdPlanParser pm = new FireBirdPlanParser(plan, session);
			FireBirdPlanNode node = null;
			try {
				node = pm.parse();
			} catch (FireBirdPlanException e) {
				throw new DBCException(e.getMessage());
			}
			rootNodes.add(node);
		}
		return rootNodes;
	}
	
}
