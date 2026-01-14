1.接口报错：
url：
http://localhost:8080/snapshot/list/null
报错信息：    
{
    "code": 500,
    "message": "Invalid bound statement (not found): com.lingflow.repository.ProcessSnapshotRepository.findByProcessDefinitionKey",
    "data": null
}

2.流程列表中点击编辑，无法正常显示
浏览器控制台错误提示：
Designer.vue:302  Failed to load existing process: TypeError: Cannot read properties of null (reading 'bpmnXml')
    at loadExistingProcess (Designer.vue:278:36)

接口:
url：
http://localhost:8080/process/definition/xml/007c4ca5-e237-11f0-9a7f-1e63a267e8f1

返回结果：
{"code":500,"message":"流程定义不存在","data":null}

3. 新建流程，应该是空白的流程设计页面，不应该有元素（现在是一个开始节点，一个任务节点，一个结束节点）

4. 创建快照之后，列表中是空的，没有展示历史快照
接口:
url：http://localhost:8080/snapshot/list/null
返回结果：
{
    "code": 500,
    "message": "Invalid bound statement (not found): com.lingflow.repository.ProcessSnapshotRepository.findByProcessDefinitionKey",
    "data": null
}

5. 数据库，通过/data/lingflow/backend/src/main/resources/db/migration中的脚本新建的表无法访问
com.intellij.database.remote.jdbc.SQLExceptionWithProperties: 错误: 对表 process_snapshot 权限不够
	at org.postgresql.core.v3.QueryExecutorImpl.receiveErrorResponse(QueryExecutorImpl.java:2725)
	at org.postgresql.core.v3.QueryExecutorImpl.processResults(QueryExecutorImpl.java:2412)
	at org.postgresql.core.v3.QueryExecutorImpl.execute(QueryExecutorImpl.java:371)
	at org.postgresql.jdbc.PgStatement.executeInternal(PgStatement.java:502)
	at org.postgresql.jdbc.PgStatement.execute(PgStatement.java:419)
	at org.postgresql.jdbc.PgStatement.executeWithFlags(PgStatement.java:341)
	at org.postgresql.jdbc.PgStatement.executeCachedSql(PgStatement.java:326)
	at org.postgresql.jdbc.PgStatement.executeWithFlags(PgStatement.java:302)
	at org.postgresql.jdbc.PgStatement.execute(PgStatement.java:297)
	in RemoteStatementImpl.execute(RemoteStatementImpl.java:79)
