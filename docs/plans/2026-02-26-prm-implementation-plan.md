# PRM Monolith MVP Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a single-deployable PRM platform that covers project, requirement, task, bug, sprint, release, dashboard, notification, and audit workflows.

**Architecture:** Use a modular monolith with strict layering (`controller -> application -> domain -> repository -> infra`). Keep SQLite as primary storage for launch and enforce MySQL-compatible schema/migrations from day one via Flyway. Implement synchronous transactional writes for core lifecycle flows and asynchronous processing for notifications and dashboard snapshots.

**Tech Stack:** Java 21, Spring Boot 3.x, Spring Security + JWT, MyBatis-Plus, Flyway, SQLite, Redis, RabbitMQ, MinIO, Spring Scheduler, SpringDoc OpenAPI, Vue3 + Element Plus.

---

### Task 1: Initialize backend project skeleton

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/prm/PrmApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/application-dev.yml`
- Create: `backend/src/main/resources/application-prod.yml`
- Test: `backend/src/test/java/com/prm/PrmApplicationTests.java`

**Step 1: Write the failing startup test**

```java
@SpringBootTest
class PrmApplicationTests {
  @Test
  void contextLoads() {}
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -f backend/pom.xml -Dtest=PrmApplicationTests test`
Expected: FAIL with project structure or dependency missing errors.

**Step 3: Write minimal implementation**

```java
@SpringBootApplication
public class PrmApplication {
  public static void main(String[] args) {
    SpringApplication.run(PrmApplication.class, args);
  }
}
```

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=PrmApplicationTests test`
Expected: PASS.

**Step 5: Commit**

```bash
git add backend/pom.xml backend/src/main backend/src/test
git commit -m "chore: bootstrap backend spring project"
```

### Task 2: Add foundational schema and Flyway migrations

**Files:**
- Create: `backend/src/main/resources/db/migration/V1__init_foundation.sql`
- Create: `backend/src/main/resources/db/migration/V2__init_pm_core.sql`
- Modify: `backend/src/main/resources/application.yml`
- Test: `backend/src/test/java/com/prm/infra/FlywayMigrationTests.java`

**Step 1: Write the failing migration test**

```java
@SpringBootTest
class FlywayMigrationTests {
  @Autowired DataSource dataSource;
  @Test
  void flywayShouldCreateCoreTables() throws Exception {
    try (var conn = dataSource.getConnection();
         var rs = conn.getMetaData().getTables(null, null, "pm_project", null)) {
      assertThat(rs.next()).isTrue();
    }
  }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -f backend/pom.xml -Dtest=FlywayMigrationTests test`
Expected: FAIL because `pm_project` does not exist.

**Step 3: Write minimal implementation**

```sql
CREATE TABLE pm_project (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name VARCHAR(128) NOT NULL,
  code VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  updated_by BIGINT NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE(code)
);
```

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=FlywayMigrationTests test`
Expected: PASS.

**Step 5: Commit**

```bash
git add backend/src/main/resources/db backend/src/test/java/com/prm/infra/FlywayMigrationTests.java backend/src/main/resources/application.yml
git commit -m "feat: add flyway core schema migrations"
```

### Task 3: Implement auth and RBAC baseline

**Files:**
- Create: `backend/src/main/java/com/prm/auth/controller/AuthController.java`
- Create: `backend/src/main/java/com/prm/auth/application/AuthApplicationService.java`
- Create: `backend/src/main/java/com/prm/auth/domain/JwtService.java`
- Create: `backend/src/main/java/com/prm/org/domain/Role.java`
- Create: `backend/src/main/java/com/prm/security/SecurityConfig.java`
- Test: `backend/src/test/java/com/prm/auth/AuthApiTests.java`

**Step 1: Write the failing auth API test**

```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthApiTests {
  @Autowired MockMvc mvc;
  @Test
  void loginShouldReturnToken() throws Exception {
    mvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
  }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -f backend/pom.xml -Dtest=AuthApiTests test`
Expected: FAIL with 404 or unauthorized.

**Step 3: Write minimal implementation**

```java
@RestController
@RequestMapping("/api/auth")
class AuthController {
  @PostMapping("/login")
  ApiResponse<TokenDto> login(@RequestBody LoginCommand command) {
    return ApiResponse.ok(authApplicationService.login(command));
  }
}
```

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=AuthApiTests test`
Expected: PASS.

**Step 5: Commit**

```bash
git add backend/src/main/java/com/prm/auth backend/src/main/java/com/prm/security backend/src/test/java/com/prm/auth/AuthApiTests.java
git commit -m "feat: implement jwt auth and baseline rbac"
```

### Task 4: Implement project management module

**Files:**
- Create: `backend/src/main/java/com/prm/project/controller/ProjectController.java`
- Create: `backend/src/main/java/com/prm/project/application/ProjectApplicationService.java`
- Create: `backend/src/main/java/com/prm/project/domain/ProjectDomainService.java`
- Create: `backend/src/main/java/com/prm/project/repository/ProjectMapper.java`
- Test: `backend/src/test/java/com/prm/project/ProjectApiTests.java`

**Step 1: Write the failing project CRUD test**

```java
@Test
void createProjectShouldSucceed() throws Exception {
  mvc.perform(post("/api/projects")
      .header("Authorization", "Bearer " + token)
      .contentType(MediaType.APPLICATION_JSON)
      .content("{\"name\":\"PRM\",\"code\":\"PRM\"}"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.data.id").isNumber());
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -f backend/pom.xml -Dtest=ProjectApiTests test`
Expected: FAIL with missing endpoint.

**Step 3: Write minimal implementation**

```java
@PostMapping
public ApiResponse<ProjectDto> create(@Valid @RequestBody CreateProjectCommand command) {
  return ApiResponse.ok(projectApplicationService.create(command));
}
```

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=ProjectApiTests test`
Expected: PASS.

**Step 5: Commit**

```bash
git add backend/src/main/java/com/prm/project backend/src/test/java/com/prm/project/ProjectApiTests.java
git commit -m "feat: implement project module and member management"
```

### Task 5: Implement requirement module and review flow

**Files:**
- Create: `backend/src/main/java/com/prm/requirement/controller/RequirementController.java`
- Create: `backend/src/main/java/com/prm/requirement/domain/RequirementStateMachine.java`
- Create: `backend/src/main/java/com/prm/requirement/repository/RequirementMapper.java`
- Test: `backend/src/test/java/com/prm/requirement/RequirementFlowTests.java`

**Step 1: Write failing status-flow test**

```java
@Test
void requirementShouldFlowDraftToApproved() {
  var machine = new RequirementStateMachine();
  assertThat(machine.canTransit("草稿", "评审中")).isTrue();
  assertThat(machine.canTransit("评审中", "已立项")).isTrue();
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -f backend/pom.xml -Dtest=RequirementFlowTests test`
Expected: FAIL because state machine does not exist.

**Step 3: Write minimal implementation**

```java
public boolean canTransit(String from, String to) {
  return transitions.getOrDefault(from, Set.of()).contains(to);
}
```

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=RequirementFlowTests test`
Expected: PASS.

**Step 5: Commit**

```bash
git add backend/src/main/java/com/prm/requirement backend/src/test/java/com/prm/requirement/RequirementFlowTests.java
git commit -m "feat: add requirement review and status flow"
```

### Task 6: Implement task module with worklog and dependency

**Files:**
- Create: `backend/src/main/java/com/prm/task/controller/TaskController.java`
- Create: `backend/src/main/java/com/prm/task/domain/TaskStateMachine.java`
- Create: `backend/src/main/java/com/prm/task/repository/TaskMapper.java`
- Create: `backend/src/main/java/com/prm/task/repository/TaskWorklogMapper.java`
- Test: `backend/src/test/java/com/prm/task/TaskWorklogTests.java`

**Step 1: Write failing worklog aggregation test**

```java
@Test
void shouldCalculateRemainingHours() {
  assertThat(service.calcRemaining(16, 6)).isEqualTo(10);
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -f backend/pom.xml -Dtest=TaskWorklogTests test`
Expected: FAIL because service method is missing.

**Step 3: Write minimal implementation**

```java
int calcRemaining(int estimate, int consumed) {
  return Math.max(estimate - consumed, 0);
}
```

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=TaskWorklogTests test`
Expected: PASS.

**Step 5: Commit**

```bash
git add backend/src/main/java/com/prm/task backend/src/test/java/com/prm/task/TaskWorklogTests.java
git commit -m "feat: implement task management and worklog"
```

### Task 7: Implement bug lifecycle module

**Files:**
- Create: `backend/src/main/java/com/prm/bug/controller/BugController.java`
- Create: `backend/src/main/java/com/prm/bug/domain/BugStateMachine.java`
- Create: `backend/src/main/java/com/prm/bug/repository/BugMapper.java`
- Create: `backend/src/main/java/com/prm/bug/repository/BugCommentMapper.java`
- Test: `backend/src/test/java/com/prm/bug/BugLifecycleTests.java`

**Step 1: Write failing bug reopen test**

```java
@Test
void closedBugCanReopen() {
  assertThat(machine.canTransit("已关闭", "新建")).isTrue();
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -f backend/pom.xml -Dtest=BugLifecycleTests test`
Expected: FAIL.

**Step 3: Write minimal implementation**

```java
transitions.put("已关闭", Set.of("新建"));
```

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=BugLifecycleTests test`
Expected: PASS.

**Step 5: Commit**

```bash
git add backend/src/main/java/com/prm/bug backend/src/test/java/com/prm/bug/BugLifecycleTests.java
git commit -m "feat: implement bug lifecycle and comments"
```

### Task 8: Implement sprint and release modules

**Files:**
- Create: `backend/src/main/java/com/prm/sprint/controller/SprintController.java`
- Create: `backend/src/main/java/com/prm/release/controller/ReleaseController.java`
- Create: `backend/src/main/java/com/prm/sprint/domain/SprintStateMachine.java`
- Test: `backend/src/test/java/com/prm/sprint/SprintCloseTests.java`

**Step 1: Write failing sprint close validation test**

```java
@Test
void closingSprintShouldRequireNoOpenCriticalBug() {
  assertThatThrownBy(() -> service.closeSprint(1L))
      .isInstanceOf(BusinessException.class);
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -f backend/pom.xml -Dtest=SprintCloseTests test`
Expected: FAIL.

**Step 3: Write minimal implementation**

```java
if (bugRepository.existsOpenCriticalInSprint(sprintId)) {
  throw new BusinessException("存在未关闭严重缺陷");
}
```

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=SprintCloseTests test`
Expected: PASS.

**Step 5: Commit**

```bash
git add backend/src/main/java/com/prm/sprint backend/src/main/java/com/prm/release backend/src/test/java/com/prm/sprint/SprintCloseTests.java
git commit -m "feat: implement sprint planning and release records"
```

### Task 9: Implement dashboard snapshots and scheduler

**Files:**
- Create: `backend/src/main/java/com/prm/dashboard/controller/DashboardController.java`
- Create: `backend/src/main/java/com/prm/dashboard/application/SnapshotAggregationJob.java`
- Create: `backend/src/main/java/com/prm/dashboard/repository/DashboardSnapshotMapper.java`
- Test: `backend/src/test/java/com/prm/dashboard/DashboardSnapshotTests.java`

**Step 1: Write failing snapshot generation test**

```java
@Test
void jobShouldGenerateTodaySnapshot() {
  job.runDailyAggregation();
  assertThat(snapshotRepository.findToday(1L)).isPresent();
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -f backend/pom.xml -Dtest=DashboardSnapshotTests test`
Expected: FAIL because snapshot job is missing.

**Step 3: Write minimal implementation**

```java
@Scheduled(cron = "0 5 0 * * ?")
public void runDailyAggregation() {
  dashboardService.aggregateAllProjects();
}
```

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=DashboardSnapshotTests test`
Expected: PASS.

**Step 5: Commit**

```bash
git add backend/src/main/java/com/prm/dashboard backend/src/test/java/com/prm/dashboard/DashboardSnapshotTests.java
git commit -m "feat: add dashboard snapshot and burndown metrics"
```

### Task 10: Implement notification and audit logging

**Files:**
- Create: `backend/src/main/java/com/prm/notify/infra/RabbitNotificationPublisher.java`
- Create: `backend/src/main/java/com/prm/audit/aspect/OperationLogAspect.java`
- Create: `backend/src/main/java/com/prm/audit/repository/OperationLogMapper.java`
- Test: `backend/src/test/java/com/prm/audit/OperationLogAspectTests.java`

**Step 1: Write failing operation-log test**

```java
@Test
void stateChangeShouldWriteOperationLog() {
  service.changeTaskStatus(1L, "进行中");
  assertThat(operationLogRepository.findLatestByBiz("TASK", 1L)).isPresent();
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -f backend/pom.xml -Dtest=OperationLogAspectTests test`
Expected: FAIL.

**Step 3: Write minimal implementation**

```java
@AfterReturning("@annotation(OperationLog)")
public void after(JoinPoint point) {
  operationLogApplicationService.record(point);
}
```

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=OperationLogAspectTests test`
Expected: PASS.

**Step 5: Commit**

```bash
git add backend/src/main/java/com/prm/notify backend/src/main/java/com/prm/audit backend/src/test/java/com/prm/audit/OperationLogAspectTests.java
git commit -m "feat: add notification eventing and operation audit"
```

### Task 11: Add frontend skeleton and API integration baseline

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/vite.config.ts`
- Create: `frontend/src/main.ts`
- Create: `frontend/src/router/index.ts`
- Create: `frontend/src/views/login/LoginPage.vue`
- Create: `frontend/src/views/project/ProjectListPage.vue`
- Create: `frontend/src/api/http.ts`
- Test: `frontend/src/api/http.spec.ts`

**Step 1: Write failing frontend API client test**

```ts
it('injects jwt token into request header', async () => {
  const cfg = await buildRequestConfig('/api/projects')
  expect(cfg.headers.Authorization).toContain('Bearer ')
})
```

**Step 2: Run test to verify it fails**

Run: `npm --prefix frontend run test -- http.spec.ts`
Expected: FAIL due to missing http utility.

**Step 3: Write minimal implementation**

```ts
export const http = axios.create({ baseURL: '/api' })
http.interceptors.request.use((cfg) => {
  const token = localStorage.getItem('accessToken')
  if (token) cfg.headers.Authorization = `Bearer ${token}`
  return cfg
})
```

**Step 4: Run test to verify it passes**

Run: `npm --prefix frontend run test -- http.spec.ts`
Expected: PASS.

**Step 5: Commit**

```bash
git add frontend
git commit -m "feat: scaffold vue admin and api client"
```

### Task 12: Wire end-to-end smoke flow and documentation

**Files:**
- Create: `docs/runbook/local-quickstart.md`
- Create: `docs/api/error-codes.md`
- Create: `backend/src/test/java/com/prm/e2e/FlowSmokeTests.java`
- Modify: `README.md`

**Step 1: Write failing smoke flow test**

```java
@Test
void shouldRunRequirementToReleaseFlow() {
  // login -> create project -> create requirement -> create task -> create bug -> close sprint -> create release
  assertThat(flowRunner.run()).isTrue();
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -f backend/pom.xml -Dtest=FlowSmokeTests test`
Expected: FAIL.

**Step 3: Write minimal implementation**

```java
public boolean run() {
  return projectId > 0 && requirementId > 0 && taskId > 0 && bugId > 0 && releaseId > 0;
}
```

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=FlowSmokeTests test`
Expected: PASS.

**Step 5: Commit**

```bash
git add docs README.md backend/src/test/java/com/prm/e2e/FlowSmokeTests.java
git commit -m "docs: add quickstart and validate e2e smoke flow"
```

---

## Verification Checklist Before Completion

- Run backend unit tests: `mvn -f backend/pom.xml test`
- Run backend style/lint if configured.
- Run frontend unit tests: `npm --prefix frontend run test`
- Build backend: `mvn -f backend/pom.xml clean package -DskipTests`
- Build frontend: `npm --prefix frontend run build`
- Verify Swagger endpoints exposed.
- Verify daily snapshot job inserts data.
- Verify JWT-authenticated RBAC on representative endpoints.

