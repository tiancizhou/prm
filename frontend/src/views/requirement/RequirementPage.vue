<template>
  <div class="app-page req-page">

    <!-- 闂佸磭鍎ら崝蹇涘疾閺屻儱鐓涢柟鑸妽濞呮粓鏌嶉悜妯哄闁哄懏鐓￠崺锟犲箛閵婏附鐝抽梺宕囧劋閸斿繘寮查弻銉ョ厸闁硅埇鍔嶅▍婊堟煃閻戞ê濮€闁哄懏鐓￠崺锟犲箛閵婏附鐝抽梺宕囧劋閸斿繘寮查弻銉ョ厸?HEADER: Title + View Toggles + Actions 闂佸磭鍎ら崝蹇涘疾閺屻儱鐓涢柟鑸妽濞呮粓鏌嶉悜妯哄闁哄懏鐓￠崺锟犲箛閵婏附鐝抽梺宕囧劋閸斿繘寮查弻銉ョ厸闁硅埇鍔嶅▍婊堟煃閻戞ê濮€闁哄懏鐓￠崺锟犲箛閵婏附鐝抽梺宕囧劋閸斿繘寮查弻銉ョ厸?-->
    <header class="page-header">
      <div class="header-left">
        <div class="title-block">
          <h1 class="page-title">{{ requirementText.pageTitle }}</h1>
          <p class="page-subtitle">{{ requirementText.pageSubtitle }}</p>
        </div>
        <div class="view-toggles">
          <el-tooltip :content="requirementText.viewLabels.list" placement="bottom">
            <el-button :type="viewMode === 'list' ? 'primary' : 'default'" size="small" circle :aria-label="requirementText.viewLabels.list" @click="viewMode = 'list'">
              <el-icon><List /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip :content="requirementText.viewLabels.kanbanSoon" placement="bottom">
            <el-button type="default" size="small" circle :aria-label="requirementText.viewLabels.kanbanSoon" disabled>
              <el-icon><Grid /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip :content="requirementText.viewLabels.timelineSoon" placement="bottom">
            <el-button type="default" size="small" circle :aria-label="requirementText.viewLabels.timelineSoon" disabled>
              <el-icon><Calendar /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </div>
      <div class="header-right page-actions">
        <el-button v-if="canManageProject" type="primary" :icon="Plus" @click="openCreate">{{ requirementText.buttons.newRequirement }}</el-button>
        <el-dropdown trigger="click" @command="handleImportExport">
          <el-button type="default" :icon="MoreFilled" circle :aria-label="requirementText.buttons.moreActions" />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="import">{{ requirementText.buttons.import }}</el-dropdown-item>
              <el-dropdown-item command="export">{{ requirementText.buttons.export }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 闂佸磭鍎ら崝蹇涘疾閺屻儱鐓涢柟鑸妽濞呮粓鏌嶉悜妯哄闁哄懏鐓￠崺锟犲箛閵婏附鐝抽梺宕囧劋閸斿繘寮查弻銉ョ厸闁硅埇鍔嶅▍婊堟煃閻戞ê濮€闁哄懏鐓￠崺锟犲箛閵婏附鐝抽梺宕囧劋閸斿繘寮查弻銉ョ厸?QUICK VIEW TABS 闂佸磭鍎ら崝蹇涘疾閺屻儱鐓涢柟鑸妽濞呮粓鏌嶉悜妯哄闁哄懏鐓￠崺锟犲箛閵婏附鐝抽梺宕囧劋閸斿繘寮查弻銉ョ厸闁硅埇鍔嶅▍婊堟煃閻戞ê濮€闁哄懏鐓￠崺锟犲箛閵婏附鐝抽梺宕囧劋閸斿繘寮查弻銉ョ厸?-->
    <div class="quick-tabs">
      <button
        v-for="tab in quickTabs"
        :key="tab.value"
        class="quick-tab"
        :class="{ active: quickView === tab.value }"
        @click="setQuickView(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 闂佸磭鍎ら崝蹇涘疾閺屻儱鐓涢柟鑸妽濞呮粓鏌嶉悜妯哄闁哄懏鐓￠崺锟犲箛閵婏附鐝抽梺宕囧劋閸斿繘寮查弻銉ョ厸闁硅埇鍔嶅▍婊堟煃閻戞ê濮€闁哄懏鐓￠崺锟犲箛閵婏附鐝抽梺宕囧劋閸斿繘寮查弻銉ョ厸?ADVANCED FILTER BAR 闂佸磭鍎ら崝蹇涘疾閺屻儱鐓涢柟鑸妽濞呮粓鏌嶉悜妯哄闁哄懏鐓￠崺锟犲箛閵婏附鐝抽梺宕囧劋閸斿繘寮查弻銉ョ厸闁硅埇鍔嶅▍婊堟煃閻戞ê濮€闁哄懏鐓￠崺锟犲箛閵婏附鐝抽梺宕囧劋閸斿繘寮查弻銉ョ厸?-->
    <div class="filter-bar">
      <el-input
        v-model="query.keyword"
        name="requirementKeyword"
        autocomplete="off"
        :placeholder="requirementText.placeholders.searchTitle"
        clearable
        class="filter-search"
        :prefix-icon="Search"
        @keyup.enter="load"
      />
      <el-select
        v-model="filterAssigneeIds"
        multiple
        collapse-tags
        collapse-tags-tooltip
        :placeholder="requirementText.placeholders.assignee"
        clearable
        class="filter-select"
        @change="load"
      >
        <el-option
          v-for="m in members"
          :key="m.userId"
          :label="m.nickname || m.username"
          :value="m.userId"
        />
      </el-select>
      <el-select
        v-model="filterSprintId"
        :placeholder="requirementText.placeholders.sprint"
        :disabled="quickView === 'unscheduled'"
        clearable
        class="filter-select"
        @change="load"
      >
        <el-option
          v-for="s in sprints"
          :key="s.id"
          :label="s.name"
          :value="s.id"
        />
      </el-select>
      <el-select
        v-model="filterStatuses"
        multiple
        collapse-tags
        collapse-tags-tooltip
        :placeholder="requirementText.placeholders.status"
        clearable
        class="filter-select"
        @change="load"
      >
        <el-option
          v-for="opt in statusOptions"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <el-button type="primary" plain @click="load">{{ requirementText.buttons.apply }}</el-button>
      <div class="filter-bar-right">
        <el-tooltip :content="requirementText.tooltips.advancedFilter" placement="bottom">
          <el-button link :icon="Filter" class="icon-btn" :aria-label="requirementText.tooltips.advancedFilter" />
        </el-tooltip>
        <el-popover placement="bottom-end" :width="220" trigger="click">
          <template #reference>
            <el-tooltip :content="requirementText.tooltips.customColumns" placement="bottom">
              <el-button link :icon="Setting" class="icon-btn" :aria-label="requirementText.tooltips.customColumns" />
            </el-tooltip>
          </template>
          <div class="column-picker">
            <div class="column-picker-title">{{ requirementText.columnPickerTitle }}</div>
            <el-checkbox-group v-model="visibleColumns">
              <div v-for="col in columnOptions" :key="col.key" class="column-picker-item">
                <el-checkbox :label="col.key">{{ col.label }}</el-checkbox>
              </div>
            </el-checkbox-group>
          </div>
        </el-popover>
      </div>
    </div>

    <!-- 闂佸磭鍎ら崝蹇涘疾閺屻儱鐓涢柟鑸妽濞呮粓鏌嶉悜妯哄闁哄懏鐓￠崺锟犲箛閵婏附鐝抽梺宕囧劋閸斿繘寮查弻銉ョ厸闁硅埇鍔嶅▍婊堟煃閻戞ê濮€闁哄懏鐓￠崺锟犲箛閵婏附鐝抽梺宕囧劋閸斿繘寮查弻銉ョ厸?DATA TABLE 闂佸磭鍎ら崝蹇涘疾閺屻儱鐓涢柟鑸妽濞呮粓鏌嶉悜妯哄闁哄懏鐓￠崺锟犲箛閵婏附鐝抽梺宕囧劋閸斿繘寮查弻銉ョ厸闁硅埇鍔嶅▍婊堟煃閻戞ê濮€闁哄懏鐓￠崺锟犲箛閵婏附鐝抽梺宕囧劋閸斿繘寮查弻銉ョ厸?-->
    <el-card class="table-card surface-card" shadow="never">
      <el-table
        ref="requirementTableRef"
        :data="displayList"
        v-loading="loading"
        row-key="id"
        class="req-table"
        @expand-change="onExpand"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="40" align="center" />
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-wrap">
              <div class="expand-header">
                <span class="expand-title">{{ requirementText.expand.relatedTasks }}</span>
                <el-button v-if="canManageProject" size="small" type="primary" :icon="Plus" @click="openTaskCreate(row)">
                  {{ requirementText.buttons.decomposeToTask }}
                </el-button>
              </div>
              <el-table
                :data="taskMap[row.id] || []"
                :loading="taskLoadingMap[row.id]"
                size="small"
                class="expand-table"
              >
                <el-table-column prop="id" label="ID" width="64" />
                <el-table-column prop="title" :label="requirementText.expand.taskTitle" min-width="180" />
                <el-table-column prop="type" :label="requirementText.expand.type" width="88" />
                <el-table-column :label="requirementText.expand.status" width="100">
                  <template #default="{ row: task }">
                    <el-tag size="small" round :type="taskStatusType(task.status)">{{ task.statusLabel }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="assigneeName" :label="requirementText.expand.assignee" width="96" />
                <el-table-column :label="requirementText.expand.hours" width="120">
                  <template #default="{ row: task }">
                    <span class="hours">{{ requirementText.expand.estimatedPrefix }} {{ task.estimatedHours }}h / {{ requirementText.expand.spentPrefix }} {{ task.spentHours }}h</span>
                  </template>
                </el-table-column>
                <el-table-column prop="dueDate" :label="requirementText.expand.due" width="110">
                  <template #default="{ row: task }">{{ formatDate(task.dueDate) || requirementText.detail.noneSymbol }}</template>
                </el-table-column>
              </el-table>
              <el-empty
                v-if="!taskLoadingMap[row.id] && !(taskMap[row.id]?.length)"
                :description="requirementText.expand.noRelatedTasks"
                :image-size="56"
                class="expand-empty"
              />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="id" label="ID" width="72" align="center" v-if="colVisible('id')">
          <template #default="{ row }">{{ row.id }}</template>
        </el-table-column>

        <el-table-column :label="requirementText.tableHeaders.title" min-width="240" v-if="colVisible('title')">
          <template #default="{ row }">
            <div class="title-cell">
              <span class="title-text">{{ row.title || requirementText.detail.noneSymbol }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="requirementText.tableHeaders.priority" width="96" v-if="colVisible('priority')">
          <template #default="{ row }">
            <el-tag size="small" round :type="priorityType(row.priority)" effect="plain" class="pill">
              {{ priorityLabel(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column :label="requirementText.tableHeaders.status" width="100" v-if="colVisible('status')">
          <template #default="{ row }">
            <el-tag size="small" round :type="statusTagType(row.status)" effect="plain" class="pill">
              {{ row.statusLabel || requirementText.detail.noneSymbol }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column :label="requirementText.tableHeaders.assignee" width="110" v-if="colVisible('assignee')">
          <template #default="{ row }">
            <div class="assignee-cell">
              <el-avatar v-if="row.assigneeName" :size="24" class="avatar">{{ (row.assigneeName || '').charAt(0) }}</el-avatar>
              <span class="assignee-name">{{ row.assigneeName || requirementText.detail.noneSymbol }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="requirementText.tableHeaders.startDate" width="108" v-if="colVisible('startDate')">
          <template #default="{ row }">{{ formatDate(row.startDate) || requirementText.detail.noneSymbol }}</template>
        </el-table-column>

        <el-table-column :label="requirementText.tableHeaders.dueDate" width="108" v-if="colVisible('dueDate')">
          <template #default="{ row }">{{ formatDate(row.dueDate) || requirementText.detail.noneSymbol }}</template>
        </el-table-column>

        <el-table-column :label="requirementText.tableHeaders.estimate" width="92" align="center" v-if="colVisible('estimate')">
          <template #default="{ row }">
            {{ row.estimatedHours != null && row.estimatedHours !== '' ? row.estimatedHours + 'h' : requirementText.detail.noneSymbol }}
          </template>
        </el-table-column>

        <el-table-column :label="requirementText.tableHeaders.actual" width="92" align="center" v-if="colVisible('actual')">
          <template #default="{ row }">
            {{ row.actualHours != null && row.actualHours !== '' ? row.actualHours + 'h' : requirementText.detail.noneSymbol }}
          </template>
        </el-table-column>

        <el-table-column :label="requirementText.tableHeaders.taskCount" width="80" align="center" v-if="colVisible('taskCount')">
          <template #default="{ row }">
            <el-tag size="small" type="info" round>{{ taskCountMap[row.id] ?? requirementText.detail.noneSymbol }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column :label="requirementText.tableHeaders.actions" min-width="180" align="right">
          <template #default="{ row }">
            <div class="action-cell">
              <el-button v-if="canViewRequirement(row)" size="small" link type="primary" @click.stop="openDetail(row)">
                {{ requirementText.buttons.view }}
              </el-button>
              <el-button v-if="canManageProject" size="small" link type="primary" @click.stop="openEdit(row)">
                {{ requirementText.buttons.edit }}
              </el-button>
              <el-button v-if="canManageProject" size="small" link type="primary" :icon="Plus" @click.stop="openTaskCreate(row)">
                {{ requirementText.buttons.decompose }}
              </el-button>
              <el-button v-if="canManageProject" size="small" link type="primary" @click.stop="openTaskTemplateDialog(row)">
                {{ taskTemplateText.applyButton }}
              </el-button>
              <el-dropdown v-if="canEditRequirement(row) && nextStatusOptions(row).length" @command="(cmd: string) => changeStatus(row, cmd)" trigger="click">
                <el-button size="small" link type="primary" class="action-status-btn">{{ requirementText.buttons.status }} <el-icon><ArrowDown /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-for="opt in nextStatusOptions(row)" :key="opt.command" :command="opt.command">
                      {{ opt.label }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <span v-if="!canViewRequirement(row)" class="readonly-hint">{{ requirementText.detail.noneSymbol }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrap">
        <span class="pagination-summary">
          {{ requirementText.pagination.showing }} {{ rangeStart }}{{ requirementText.pagination.to }}{{ rangeEnd }} {{ requirementText.pagination.of }} {{ paginationTotal }} {{ requirementText.pagination.items }}
        </span>
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="paginationTotal"
          :page-sizes="[10, 20, 50, 100]"
          layout="sizes, prev, pager, next"
          small
          @current-change="load"
          @size-change="load"
        />
      </div>
    </el-card>

    <!-- New Requirement Dialog -->
    <el-dialog v-model="showCreate" :title="requirementText.dialogs.createRequirement" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="88px">
        <el-form-item :label="requirementText.formLabels.requirementTitle" prop="title" required>
          <el-input v-model="form.title" :placeholder="requirementText.placeholders.requirementTitle" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item :label="requirementText.formLabels.priority">
              <el-select v-model="form.priority" :placeholder="requirementText.placeholders.priority" class="full-width-control">
                <el-option v-for="opt in requirementPriorityOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="requirementText.formLabels.estimatedHours">
              <el-input-number v-model="form.estimatedHours" :min="0" :precision="1" class="full-width-control" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item :label="requirementText.formLabels.startDate">
              <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" :placeholder="requirementText.placeholders.chooseStartDate" class="full-width-control" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="requirementText.formLabels.dueDate">
              <el-date-picker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD" :placeholder="requirementText.placeholders.chooseDueDate" class="full-width-control" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="requirementText.formLabels.description">
          <el-input v-model="form.description" type="textarea" :rows="3" :placeholder="requirementText.placeholders.optional" />
        </el-form-item>
        <el-form-item :label="requirementText.formLabels.acceptanceCriteria">
          <el-input v-model="form.acceptanceCriteria" type="textarea" :rows="2" :placeholder="requirementText.placeholders.optional" />
        </el-form-item>
        <el-form-item :label="requirementText.formLabels.attachments">
          <el-upload
            v-model:file-list="uploadFileList"
            :auto-upload="false"
            :limit="10"
            :on-exceed="() => ElMessage.warning(requirementText.upload.maxFilesWarning)"
            multiple
            drag
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">{{ requirementText.upload.dropText }}<em>{{ requirementText.upload.clickText }}</em></div>
            <template #tip>
              <div class="el-upload__tip">{{ requirementText.upload.tip }}</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">{{ requirementText.buttons.cancel }}</el-button>
        <el-button type="primary" :loading="creating" @click="create">{{ requirementText.buttons.create }}</el-button>
      </template>
    </el-dialog>

    <!-- Edit Requirement Dialog -->
    <el-dialog v-model="showEdit" :title="requirementText.dialogs.editRequirement" width="600px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="formRules" label-width="88px">
        <el-form-item :label="requirementText.formLabels.requirementTitle" prop="title" required>
          <el-input v-model="editForm.title" :placeholder="requirementText.placeholders.requirementTitle" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item :label="requirementText.formLabels.priority">
              <el-select v-model="editForm.priority" :placeholder="requirementText.placeholders.priority" class="full-width-control">
                <el-option v-for="opt in requirementPriorityOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="requirementText.formLabels.estimatedHours">
              <el-input-number v-model="editForm.estimatedHours" :min="0" :precision="1" class="full-width-control" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item :label="requirementText.formLabels.owner">
              <el-select v-model="editForm.assigneeId" :placeholder="requirementText.placeholders.assignee" clearable class="full-width-control">
                <el-option v-for="m in members" :key="m.userId" :label="m.nickname || m.username" :value="m.userId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="requirementText.formLabels.sprint">
              <el-select v-model="editForm.sprintId" :placeholder="requirementText.placeholders.sprint" clearable class="full-width-control">
                <el-option v-for="s in sprints" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item :label="requirementText.formLabels.startDate">
              <el-date-picker v-model="editForm.startDate" type="date" value-format="YYYY-MM-DD" :placeholder="requirementText.placeholders.chooseStartDate" class="full-width-control" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="requirementText.formLabels.dueDate">
              <el-date-picker v-model="editForm.dueDate" type="date" value-format="YYYY-MM-DD" :placeholder="requirementText.placeholders.chooseDueDate" class="full-width-control" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="requirementText.formLabels.description">
          <el-input v-model="editForm.description" type="textarea" :rows="3" :placeholder="requirementText.placeholders.optional" />
        </el-form-item>
        <el-form-item :label="requirementText.formLabels.acceptanceCriteria">
          <el-input v-model="editForm.acceptanceCriteria" type="textarea" :rows="2" :placeholder="requirementText.placeholders.optional" />
        </el-form-item>
        <el-form-item :label="requirementText.formLabels.attachments">
          <div class="edit-attachments">
            <div v-if="editAttachments.length" class="attachment-list">
              <div v-for="att in editAttachments" :key="att.id" class="attachment-item">
                <el-icon class="att-icon"><Document /></el-icon>
                <span class="att-name" :title="att.filename">{{ att.filename }}</span>
                <span class="att-size">{{ formatFileSize(att.fileSize) }}</span>
                <el-button type="danger" link size="small" @click="removeEditAttachment(att)">{{ requirementText.buttons.delete }}</el-button>
              </div>
            </div>
            <el-upload
              :show-file-list="false"
              :before-upload="(file) => { uploadEditAttachment(file); return false }"
              :limit="10"
              accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.png,.jpg,.jpeg,.gif,.zip"
              multiple
            >
              <el-button type="primary" plain size="small" :loading="editUploading">
                {{ requirementText.buttons.uploadNewAttachment }}
              </el-button>
            </el-upload>
            <div class="el-upload__tip upload-tip-spaced">{{ requirementText.upload.tip }}</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">{{ requirementText.buttons.cancel }}</el-button>
        <el-button type="primary" :loading="updating" @click="saveEdit">{{ requirementText.buttons.save }}</el-button>
      </template>
    </el-dialog>

    <!-- Requirement Detail Dialog -->
    <el-dialog v-model="showDetail" :title="requirementText.dialogs.requirementDetail" width="620px" destroy-on-close>
      <div v-if="detailReq" class="detail-body">
        <div class="detail-section">
          <h3 class="detail-title">{{ detailReq.title }}</h3>
          <div class="detail-meta">
            <el-tag size="small" round :type="statusTagType(detailReq.status)">{{ detailReq.statusLabel }}</el-tag>
            <el-tag size="small" round :type="priorityType(detailReq.priority)" effect="plain">{{ priorityLabel(detailReq.priority) }}</el-tag>
          </div>
          <div v-if="canManageProject" class="detail-actions">
            <el-button type="primary" plain size="small" @click="openTaskTemplateDialog(detailReq)">
              {{ taskTemplateText.applyButton }}
            </el-button>
          </div>
        </div>
        <el-descriptions :column="2" border size="small" class="detail-desc">
          <el-descriptions-item :label="requirementText.formLabels.owner">{{ detailReq.assigneeName || requirementText.detail.noneSymbol }}</el-descriptions-item>
          <el-descriptions-item :label="requirementText.detail.estimatedHours">{{ detailReq.estimatedHours != null ? detailReq.estimatedHours + 'h' : requirementText.detail.noneSymbol }}</el-descriptions-item>
          <el-descriptions-item :label="requirementText.detail.actualHours">{{ detailReq.actualHours != null ? detailReq.actualHours + 'h' : requirementText.detail.noneSymbol }}</el-descriptions-item>
          <el-descriptions-item :label="requirementText.formLabels.actualStartAt">{{ formatDateTime(detailReq.actualStartAt) || requirementText.detail.noneSymbol }}</el-descriptions-item>
          <el-descriptions-item :label="requirementText.formLabels.actualEndAt">{{ formatDateTime(detailReq.actualEndAt) || requirementText.detail.noneSymbol }}</el-descriptions-item>
          <el-descriptions-item :label="requirementText.formLabels.startDate">{{ formatDate(detailReq.startDate) || requirementText.detail.noneSymbol }}</el-descriptions-item>
          <el-descriptions-item :label="requirementText.formLabels.dueDate">{{ formatDate(detailReq.dueDate) || requirementText.detail.noneSymbol }}</el-descriptions-item>
          <el-descriptions-item :label="requirementText.formLabels.description" :span="2">{{ detailReq.description || requirementText.detail.noneSymbol }}</el-descriptions-item>
          <el-descriptions-item :label="requirementText.formLabels.acceptanceCriteria" :span="2">{{ detailReq.acceptanceCriteria || requirementText.detail.noneSymbol }}</el-descriptions-item>
          <el-descriptions-item :label="doneVerificationText.scenarioLabel" :span="2">{{ detailReq.verificationScenario || requirementText.detail.noneSymbol }}</el-descriptions-item>
          <el-descriptions-item :label="doneVerificationText.stepsLabel" :span="2">{{ detailReq.verificationSteps || requirementText.detail.noneSymbol }}</el-descriptions-item>
          <el-descriptions-item :label="doneVerificationText.resultLabel" :span="2">{{ detailReq.verificationResult || requirementText.detail.noneSymbol }}</el-descriptions-item>
        </el-descriptions>
        <div class="detail-section">
          <div class="detail-section-title">{{ requirementText.detail.attachments }}</div>
          <div v-if="detailAttachments.length" class="attachment-list">
            <div v-for="att in detailAttachments" :key="att.id" class="attachment-item">
              <el-icon class="att-icon"><Document /></el-icon>
              <span class="att-name" :title="att.filename">{{ att.filename }}</span>
              <span class="att-size">{{ formatFileSize(att.fileSize) }}</span>
              <el-button type="primary" link size="small" @click="downloadAttachment(att)">{{ requirementText.buttons.download }}</el-button>
            </div>
          </div>
          <el-empty v-else :description="requirementText.detail.noAttachments" :image-size="50" />
        </div>
      </div>
      <template #footer>
        <el-button v-if="canManageProject && detailReq" type="primary" plain @click="openTaskTemplateDialog(detailReq)">
          {{ taskTemplateText.applyButton }}
        </el-button>
        <el-button @click="showDetail = false">{{ requirementText.buttons.close }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDoneStatusDialog" :title="requirementText.dialogs.doneHoursTitle" width="520px" destroy-on-close>
      <el-form :model="doneStatusForm" label-width="100px">
        <el-form-item :label="requirementText.formLabels.actualStartAt" required>
          <el-date-picker
            v-model="doneStatusForm.actualStartAt"
            type="datetime"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :placeholder="requirementText.placeholders.doneStartAt"
            class="full-width-control"
          />
        </el-form-item>
        <el-form-item :label="requirementText.formLabels.actualEndAt" required>
          <el-date-picker
            v-model="doneStatusForm.actualEndAt"
            type="datetime"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :placeholder="requirementText.placeholders.doneEndAt"
            class="full-width-control"
          />
        </el-form-item>
        <el-form-item :label="doneVerificationText.scenarioLabel" required>
          <el-input v-model="doneStatusForm.verificationScenario" type="textarea" :rows="2" :placeholder="doneVerificationText.scenarioPlaceholder" />
        </el-form-item>
        <el-form-item :label="doneVerificationText.stepsLabel" required>
          <el-input v-model="doneStatusForm.verificationSteps" type="textarea" :rows="3" :placeholder="doneVerificationText.stepsPlaceholder" />
        </el-form-item>
        <el-form-item :label="doneVerificationText.resultLabel" required>
          <el-input v-model="doneStatusForm.verificationResult" type="textarea" :rows="2" :placeholder="doneVerificationText.resultPlaceholder" />
        </el-form-item>
        <el-form-item :label="doneScreenshotText.screenshotsLabel">
          <el-upload
            v-model:file-list="doneScreenshotFileList"
            :auto-upload="false"
            :before-upload="beforeDoneScreenshotUpload"
            :show-file-list="true"
            accept="image/*"
            multiple
          >
            <el-button type="primary" plain :icon="UploadFilled" :loading="doneStatusSubmitting">{{ doneScreenshotText.uploadScreenshots }}</el-button>
          </el-upload>
          <div class="form-tip">{{ doneScreenshotText.screenshotsHint }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDoneStatusDialog">{{ requirementText.buttons.cancel }}</el-button>
        <el-button type="primary" :loading="doneStatusSubmitting" @click="submitDoneStatus">{{ requirementText.buttons.save }}</el-button>
      </template>
    </el-dialog>

    <!-- Decompose Task Dialog -->
    <el-dialog v-model="showTaskCreate" :title="`${requirementText.dialogs.decomposeTaskPrefix} / ${currentReq?.title || ''}`" width="560px" destroy-on-close>
      <el-form :model="taskForm" label-width="100px">
        <el-form-item :label="requirementText.formLabels.belongsRequirement">
          <el-input :model-value="currentReq?.title" disabled />
        </el-form-item>
        <el-form-item :label="requirementText.formLabels.taskTitle" required>
          <el-input v-model="taskForm.title" :placeholder="requirementText.placeholders.taskTitle" />
        </el-form-item>
        <el-form-item :label="requirementText.formLabels.taskType">
          <el-select v-model="taskForm.type">
            <el-option v-for="opt in taskTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="requirementText.formLabels.priority">
          <el-select v-model="taskForm.priority">
            <el-option v-for="opt in taskPriorityOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="requirementText.formLabels.owner">
          <el-select v-model="taskForm.assigneeId" clearable filterable :placeholder="requirementText.placeholders.assignee" class="full-width-control">
            <el-option v-for="m in members" :key="m.userId" :label="m.nickname || m.username" :value="m.userId" />
          </el-select>
        </el-form-item>
        <el-form-item :label="requirementText.formLabels.estimatedHours">
          <el-input-number v-model="taskForm.estimatedHours" :min="0" :precision="1" />
        </el-form-item>
        <el-form-item :label="requirementText.formLabels.dueDate">
          <el-date-picker v-model="taskForm.dueDate" type="date" value-format="YYYY-MM-DD" class="full-width-control" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTaskCreate = false">{{ requirementText.buttons.cancel }}</el-button>
        <el-button type="primary" :loading="creatingTask" @click="createTask">{{ requirementText.buttons.createTask }}</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showTaskTemplateDialog"
      :title="`${taskTemplateText.dialogTitle} / ${currentReq?.title || ''}`"
      width="980px"
      destroy-on-close
    >
      <div class="template-toolbar">
        <el-select v-model="taskTemplateKey" class="template-select" @change="applySelectedTaskTemplate">
          <el-option
            v-for="template in taskTemplateOptions"
            :key="template.value"
            :label="template.label"
            :value="template.value"
          />
        </el-select>
        <el-button type="primary" plain @click="addTaskTemplateRow">{{ taskTemplateText.addRow }}</el-button>
      </div>
      <div class="form-tip">{{ taskTemplateText.hint }}</div>

      <el-table :data="taskTemplateRows" size="small" class="template-table" max-height="360">
        <el-table-column :label="taskTemplateText.columns.title" min-width="240">
          <template #default="{ row }">
            <el-input v-model="row.title" :placeholder="requirementText.placeholders.taskTitle" maxlength="120" />
          </template>
        </el-table-column>
        <el-table-column :label="taskTemplateText.columns.type" width="110">
          <template #default="{ row }">
            <el-select v-model="row.type">
              <el-option v-for="opt in taskTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column :label="taskTemplateText.columns.priority" width="110">
          <template #default="{ row }">
            <el-select v-model="row.priority">
              <el-option v-for="opt in taskPriorityOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column :label="taskTemplateText.columns.assignee" width="150">
          <template #default="{ row }">
            <el-select v-model="row.assigneeId" clearable filterable :placeholder="requirementText.placeholders.assignee" class="full-width-control">
              <el-option v-for="m in members" :key="m.userId" :label="m.nickname || m.username" :value="m.userId" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column :label="taskTemplateText.columns.estimatedHours" width="120">
          <template #default="{ row }">
            <el-input-number v-model="row.estimatedHours" :min="0" :precision="1" :step="0.5" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column :label="taskTemplateText.columns.dueDate" width="150">
          <template #default="{ row }">
            <el-date-picker v-model="row.dueDate" type="date" value-format="YYYY-MM-DD" class="full-width-control" />
          </template>
        </el-table-column>
        <el-table-column :label="taskTemplateText.columns.actions" width="80" align="center">
          <template #default="{ $index }">
            <el-button type="danger" link @click="removeTaskTemplateRow($index)">{{ requirementText.buttons.delete }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="showTaskTemplateDialog = false">{{ requirementText.buttons.cancel }}</el-button>
        <el-button type="primary" :loading="taskTemplateSubmitting" @click="submitTaskTemplate">{{ taskTemplateText.submitButton }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { Plus, Search, List, Grid, Calendar, MoreFilled, Filter, Setting, ArrowDown, UploadFilled, Document } from '@element-plus/icons-vue'
import { requirementApi } from '@/api/requirement'
import { taskApi } from '@/api/task'
import { projectApi } from '@/api/project'
import { sprintApi } from '@/api/sprint'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useProjectStore } from '@/stores/project'
import { REQUIREMENT_I18N } from '@/constants/requirement'
import { resolveThemeLocale } from '@/constants/theme'

const authStore = useAuthStore()
const projectStore = useProjectStore()
const route = useRoute()
const projectId = Number(route.params.id)
const currentUserId = computed(() => authStore.user?.userId ?? null)
const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const requirementText = REQUIREMENT_I18N[currentLocale]
const doneVerificationText = currentLocale === 'zh-CN'
  ? {
      scenarioLabel: '验证场景',
      stepsLabel: '验证步骤',
      resultLabel: '实际结果',
      conclusionLabel: '结论',
      methodLabel: '验证方式（可选）',
      scenarioPlaceholder: '例：登录后创建需求并指派成员',
      stepsPlaceholder: '至少填写 1 条步骤，例如：1. 创建需求 2. 指派成员',
      resultPlaceholder: '例：页面保存成功，列表展示正确',
      conclusionPlaceholder: '例：通过',
      methodPlaceholder: '例：自测/联调/回归',
      requiredMessage: '完成需求时请补全验证结果模板'
    }
  : {
      scenarioLabel: 'Verification Scenario',
      stepsLabel: 'Verification Steps',
      resultLabel: 'Actual Result',
      conclusionLabel: 'Conclusion',
      methodLabel: 'Verification Method (Optional)',
      scenarioPlaceholder: 'e.g. Create requirement and assign member after login',
      stepsPlaceholder: 'At least 1 step, e.g. 1) Create requirement 2) Assign member',
      resultPlaceholder: 'e.g. Save succeeds and list displays correctly',
      conclusionPlaceholder: 'e.g. Pass',
      methodPlaceholder: 'e.g. Self-test / Joint test / Regression',
      requiredMessage: 'Please complete the verification template when marking done'
    }

const doneRequiredMessage = currentLocale === 'zh-CN'
  ? '完成需求时请补全验证场景、步骤和实际结果'
  : 'Please complete scenario, steps, and result when marking done'

const doneScreenshotText = currentLocale === 'zh-CN'
  ? {
      screenshotsLabel: '验证截图（可选）',
      uploadScreenshots: '上传截图',
      screenshotsHint: '支持多张图片，非必填',
      screenshotTypeInvalid: '只能上传图片文件',
      screenshotUploadPartialFailed: '部分截图上传失败，请稍后重试'
    }
  : {
      screenshotsLabel: 'Screenshots (Optional)',
      uploadScreenshots: 'Upload Screenshots',
      screenshotsHint: 'Multiple images are supported and optional',
      screenshotTypeInvalid: 'Only image files are allowed',
      screenshotUploadPartialFailed: 'Some screenshots failed to upload, please retry later'
    }

const taskTemplateText = currentLocale === 'zh-CN'
  ? {
      applyButton: '套用任务模板',
      dialogTitle: '任务模板',
      addRow: '新增一行',
      submitButton: '批量创建任务',
      hint: '可手动调整每条任务后再提交，至少保留 1 条有效任务。',
      columns: {
        title: '任务标题',
        type: '类型',
        priority: '优先级',
        assignee: '负责人',
        estimatedHours: '预估工时',
        dueDate: '截止日期',
        actions: '操作'
      },
      templates: {
        feature: '功能开发模板',
        bugfix: '缺陷修复模板'
      },
      messages: {
        titleRequired: '请至少填写 1 条任务标题',
        estimatedInvalid: '预估工时不能小于 0',
        createSuccess: '模板任务创建成功',
        createPartialFailed: '部分任务创建失败，请检查后重试',
        createFailed: '模板任务创建失败'
      }
    }
  : {
      applyButton: 'Apply Task Template',
      dialogTitle: 'Task Template',
      addRow: 'Add Row',
      submitButton: 'Create Tasks in Batch',
      hint: 'You can adjust each task before submit. Keep at least one valid row.',
      columns: {
        title: 'Task Title',
        type: 'Type',
        priority: 'Priority',
        assignee: 'Assignee',
        estimatedHours: 'Est. Hours',
        dueDate: 'Due Date',
        actions: 'Actions'
      },
      templates: {
        feature: 'Feature Development',
        bugfix: 'Bugfix Handling'
      },
      messages: {
        titleRequired: 'Please keep at least one task title',
        estimatedInvalid: 'Estimated hours must be greater than or equal to 0',
        createSuccess: 'Template tasks created successfully',
        createPartialFailed: 'Some tasks failed to create, please retry',
        createFailed: 'Failed to create template tasks'
      }
    }

const canManageProject = computed(() => {
  const currentProject = projectStore.currentProject
  return currentProject?.id === projectId && currentProject?.canEdit === true
})

// View mode & quick view
const viewMode = ref<'list' | 'kanban' | 'timeline'>('list')
type QuickView = 'all' | 'assigned' | 'due_week' | 'unscheduled'
const quickView = ref<QuickView>('all')

const quickTabs: Array<{ value: QuickView; label: string }> = [
  { value: 'all', label: requirementText.quickTabs.all },
  { value: 'assigned', label: requirementText.quickTabs.assigned },
  { value: 'due_week', label: requirementText.quickTabs.due_week },
  { value: 'unscheduled', label: requirementText.quickTabs.unscheduled }
]

// Filter state (multi-select values)
const filterAssigneeIds = ref<number[]>([])
const filterSprintId = ref<number | null>(null)
const filterStatuses = ref<string[]>([])
const query = reactive({
  page: 1,
  size: 20,
  projectId,
  status: '' as string,
  keyword: '',
  assigneeId: null as number | null,
  sprintId: null as number | null,
  unscheduled: false
})

function toLocalDateText(date: Date): string {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function getCurrentWeekDateRange() {
  const now = new Date()
  const today = new Date(now)
  today.setHours(0, 0, 0, 0)
  const dayOfWeek = now.getDay() === 0 ? 7 : now.getDay()
  const monday = new Date(now)
  monday.setDate(now.getDate() - dayOfWeek + 1)
  monday.setHours(0, 0, 0, 0)
  const sunday = new Date(monday)
  sunday.setDate(monday.getDate() + 6)
  sunday.setHours(23, 59, 59, 999)
  return {
    today,
    monday,
    sunday,
    fromText: toLocalDateText(today),
    toText: toLocalDateText(sunday)
  }
}

function parseDateOnly(value: string | null | undefined): Date | null {
  if (!value) return null
  const text = String(value).slice(0, 10)
  const parts = text.split('-').map(Number)
  if (parts.length !== 3 || parts.some(n => !Number.isFinite(n))) return null
  const [year, month, day] = parts
  if (!year || !month || !day) return null
  const date = new Date(year, month - 1, day)
  if (Number.isNaN(date.getTime())) return null
  date.setHours(0, 0, 0, 0)
  return date
}

// Build API params from filters
function buildParams() {
  const status = filterStatuses.value.length === 1 ? filterStatuses.value[0] : filterStatuses.value.length > 1 ? undefined : query.status

  // Due this week: today to Sunday
  let dueDateFrom: string | undefined
  let dueDateTo: string | undefined
  if (quickView.value === 'due_week') {
    const weekRange = getCurrentWeekDateRange()
    dueDateFrom = weekRange.fromText
    dueDateTo = weekRange.toText
  }

  return {
    page: query.page,
    size: query.size,
    projectId,
    keyword: query.keyword || undefined,
    status: status || undefined,
    assigneeId: quickView.value === 'assigned' ? currentUserId.value : filterAssigneeIds.value[0] ?? undefined,
    sprintId: quickView.value === 'unscheduled' ? undefined : (filterSprintId.value ?? undefined),
    unscheduled: quickView.value === 'unscheduled' ? true : undefined,
    dueDateFrom,
    dueDateTo
  }
}

function setQuickView(v: QuickView) {
  if (v === 'assigned' && !currentUserId.value) {
    ElMessage.warning(requirementText.messages.noEditPermission)
    return
  }
  if (v === 'unscheduled') {
    filterSprintId.value = null
  }
  quickView.value = v
  query.page = 1
  load()
}

const statusOptions = [
  ...requirementText.statusOptions
]

const requirementPriorityOptions = [
  { value: 'LOW', label: requirementText.priorityLabels.LOW ?? 'LOW' },
  { value: 'MEDIUM', label: requirementText.priorityLabels.MEDIUM ?? 'MEDIUM' },
  { value: 'HIGH', label: requirementText.priorityLabels.HIGH ?? 'HIGH' },
  { value: 'CRITICAL', label: requirementText.priorityLabels.CRITICAL ?? 'CRITICAL' }
]

const taskTypeOptions = [...requirementText.taskTypeOptions]
const taskPriorityOptions = [...requirementText.taskPriorityOptions]

// Column visibility (customize columns)
const columnOptions = [
  ...requirementText.columnOptions
]

const visibleColumns = ref<string[]>(['title', 'priority', 'status', 'assignee', 'dueDate', 'estimate', 'actual', 'taskCount'])
const requirementTableRef = ref<any>(null)

function colVisible(key: string) {
  return visibleColumns.value.includes(key)
}

watch(visibleColumns, async () => {
  await nextTick()
  requirementTableRef.value?.doLayout?.()
}, { deep: true })

// Data
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const members = ref<any[]>([])
const sprints = ref<any[]>([])

const taskMap = reactive<Record<number, any[]>>({})
const taskLoadingMap = reactive<Record<number, boolean>>({})
const taskCountMap = reactive<Record<number, number>>({})

// When multiple statuses selected, filter client-side (API only supports single status)
const displayList = computed(() => {
  let rows = list.value

  if (quickView.value === 'assigned' && currentUserId.value != null) {
    rows = rows.filter(r => r.assigneeId != null && r.assigneeId === currentUserId.value)
  }

  if (quickView.value === 'unscheduled') {
    rows = rows.filter(r => r.sprintId == null && !parseDateOnly(r?.startDate) && !parseDateOnly(r?.dueDate))
  }

  if (quickView.value === 'due_week') {
    const { today, sunday } = getCurrentWeekDateRange()
    rows = rows.filter(r => {
      const dueDate = parseDateOnly(r?.dueDate)
      if (!dueDate) return false
      return dueDate.getTime() >= today.getTime() && dueDate.getTime() <= sunday.getTime()
    })
  }

  if (filterStatuses.value.length > 1) {
    const set = new Set(filterStatuses.value)
    rows = rows.filter(r => set.has(r.status))
  }
  if (filterAssigneeIds.value.length > 1) {
    const set = new Set(filterAssigneeIds.value)
    rows = rows.filter(r => r.assigneeId != null && set.has(r.assigneeId))
  }
  return rows
})

const paginationTotal = computed(() => {
  if (total.value >= list.value.length) return total.value
  return list.value.length
})

const rangeStart = computed(() => paginationTotal.value === 0 ? 0 : (query.page - 1) * query.size + 1)
const rangeEnd = computed(() => Math.min(query.page * query.size, paginationTotal.value))

// Selection for bulk actions
const selectedRows = ref<any[]>([])

function handleSelectionChange(rows: any[]) {
  selectedRows.value = rows
}

function handleImportExport(cmd: string) {
  if (cmd === 'import') ElMessage.info(requirementText.messages.importSoon)
  else if (cmd === 'export') ElMessage.info(requirementText.messages.exportSoon)
}

// Labels & types
const priorityMap: Record<string, string> = requirementText.priorityLabels
type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'
const priorityTypeMap: Record<string, TagType> = { LOW: 'info', MEDIUM: 'primary', HIGH: 'warning', CRITICAL: 'danger' }
const statusTagMap: Record<string, TagType> = {
  DRAFT: 'info', IN_PROGRESS: 'warning', DONE: 'success'
}
const taskStatusTypeMap: Record<string, TagType> = { TODO: 'info', IN_PROGRESS: 'warning', PENDING_REVIEW: 'primary', DONE: 'success', CLOSED: 'info' }

function priorityLabel(p: string) { return priorityMap[p] || p || requirementText.detail.noneSymbol }
function priorityType(p: string): TagType { return priorityTypeMap[p] || 'info' }
function statusTagType(s: string): TagType { return statusTagMap[s] || 'info' }
function taskStatusType(s: string): TagType { return taskStatusTypeMap[s] || 'info' }

/** Three-state flow: Todo -> Start, In Progress -> Done, Done -> Reopen */
function nextStatusOptions(row: any): { command: string; label: string }[] {
  const s = row?.status
  if (s === 'DRAFT' || s === 'REVIEWING' || s === 'APPROVED') {
    return [{ command: 'IN_PROGRESS', label: requirementText.nextStatusLabels.start }]
  }
  if (s === 'IN_PROGRESS') return [{ command: 'DONE', label: requirementText.nextStatusLabels.markDone }]
  if (s === 'DONE' || s === 'CLOSED') return [{ command: 'IN_PROGRESS', label: requirementText.nextStatusLabels.reopen }]
  return []
}

/** Whether the current user can view this requirement (manager or assignee) */
function canViewRequirement(row: any): boolean {
  if (canManageProject.value) return true
  return row?.assigneeId != null && row.assigneeId === currentUserId.value
}

function canEditRequirement(row: any): boolean {
  if (canManageProject.value) return true
  return row?.assigneeId != null && row.assigneeId === currentUserId.value
}

async function ensureProjectPermission() {
  const currentProject = projectStore.currentProject
  if (currentProject?.id === projectId && typeof currentProject.canEdit === 'boolean') {
    return
  }
  const response = await projectApi.get(projectId)
  projectStore.setCurrentProject((response as any).data)
}

async function openDetail(row: any) {
  try {
    const res = await requirementApi.get(row.id)
    detailReq.value = (res as any).data
    const attRes = await requirementApi.listAttachments(row.id)
    detailAttachments.value = (attRes as any).data ?? []
    showDetail.value = true
  } catch {
    ElMessage.error(requirementText.messages.loadDetailFailed)
  }
}

async function downloadAttachment(att: any) {
  if (!detailReq.value?.id) return
  try {
    await requirementApi.downloadAttachment(detailReq.value.id, att.id, att.filename)
    ElMessage.success(requirementText.messages.startDownload)
  } catch {
    ElMessage.error(requirementText.messages.downloadFailed)
  }
}

function formatDate(d: string | null | undefined): string {
  if (!d) return ''
  const date = parseDateOnly(d)
  if (!date) return d
  return toLocalDateText(date)
}

function formatDateTime(d: string | null | undefined): string {
  if (!d) return ''
  const date = new Date(d)
  if (isNaN(date.getTime())) return d
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${min}`
}

function formatFileSize(bytes: number | null | undefined): string {
  if (bytes == null || bytes === 0) return requirementText.detail.noneSymbol
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

// New requirement form
const showCreate = ref(false)
const creating = ref(false)
const formRef = ref()
const uploadFileList = ref<any[]>([])

const showEdit = ref(false)
const updating = ref(false)
const editFormRef = ref()
const editingReq = ref<any>(null)
const editAttachments = ref<any[]>([])
const editUploading = ref(false)

watch(showEdit, (visible) => {
  if (!visible) {
    return
  }
  if (canManageProject.value) {
    return
  }
  showEdit.value = false
  editingReq.value = null
  ElMessage.warning(requirementText.messages.noManagePermission)
})

watch(canManageProject, (allowed) => {
  if (allowed || !showEdit.value) {
    return
  }
  showEdit.value = false
  editingReq.value = null
  ElMessage.warning(requirementText.messages.noManagePermission)
})

const showDetail = ref(false)
const detailReq = ref<any>(null)
const detailAttachments = ref<any[]>([])

const editForm = reactive({
  title: '',
  description: '',
  priority: 'MEDIUM',
  assigneeId: null as number | null,
  sprintId: null as number | null,
  estimatedHours: 0,
  acceptanceCriteria: '',
  startDate: '',
  dueDate: ''
})

const form = reactive({
  title: '',
  description: '',
  priority: 'MEDIUM',
  estimatedHours: 0,
  acceptanceCriteria: '',
  startDate: '',
  dueDate: ''
})
const formRules = { title: [{ required: true, message: requirementText.messages.titleRequired, trigger: 'blur' }] }

// Decompose task
const showTaskCreate = ref(false)
const creatingTask = ref(false)
const currentReq = ref<any>(null)
const taskForm = reactive({ title: '', type: 'TASK', priority: 'MEDIUM', assigneeId: null as number | null, estimatedHours: 0, dueDate: '' })

type TaskTemplateKey = 'feature' | 'bugfix'

type TaskTemplateRow = {
  title: string
  type: 'TASK' | 'SUBTASK' | 'TECH'
  priority: 'LOW' | 'MEDIUM' | 'HIGH'
  assigneeId: number | null
  estimatedHours: number
  dueDate: string
}

const showTaskTemplateDialog = ref(false)
const taskTemplateSubmitting = ref(false)
const taskTemplateKey = ref<TaskTemplateKey>('feature')
const taskTemplateRows = ref<TaskTemplateRow[]>([])

const taskTemplateOptions: Array<{ value: TaskTemplateKey; label: string }> = [
  { value: 'feature', label: taskTemplateText.templates.feature },
  { value: 'bugfix', label: taskTemplateText.templates.bugfix }
]

const taskTemplatePresets: Record<TaskTemplateKey, TaskTemplateRow[]> = {
  feature: [
    { title: currentLocale === 'zh-CN' ? '需求澄清与边界确认' : 'Clarify requirement scope', type: 'TASK', priority: 'MEDIUM', assigneeId: null, estimatedHours: 1, dueDate: '' },
    { title: currentLocale === 'zh-CN' ? '核心开发实现' : 'Core implementation', type: 'TASK', priority: 'HIGH', assigneeId: null, estimatedHours: 4, dueDate: '' },
    { title: currentLocale === 'zh-CN' ? '联调与自测' : 'Integration and self-test', type: 'TASK', priority: 'MEDIUM', assigneeId: null, estimatedHours: 2, dueDate: '' }
  ],
  bugfix: [
    { title: currentLocale === 'zh-CN' ? '复现与根因定位' : 'Reproduce and root-cause analysis', type: 'TASK', priority: 'HIGH', assigneeId: null, estimatedHours: 1, dueDate: '' },
    { title: currentLocale === 'zh-CN' ? '修复与回归验证' : 'Fix and regression verification', type: 'TASK', priority: 'HIGH', assigneeId: null, estimatedHours: 2, dueDate: '' },
    { title: currentLocale === 'zh-CN' ? '发布说明与同步' : 'Release note and sync', type: 'SUBTASK', priority: 'MEDIUM', assigneeId: null, estimatedHours: 0.5, dueDate: '' }
  ]
}

const showDoneStatusDialog = ref(false)
const doneStatusSubmitting = ref(false)
const doneStatusTarget = ref<any>(null)
const doneScreenshotFileList = ref<any[]>([])
const doneStatusForm = reactive({
  actualStartAt: '',
  actualEndAt: '',
  verificationScenario: '',
  verificationSteps: '',
  verificationResult: ''
})

async function load() {
  loading.value = true
  try {
    const params = buildParams() as any
    const res = await requirementApi.list(params)
    const data = (res as any).data
    list.value = data.records || []
    const parsedTotal = Number(data.total)
    total.value = Number.isFinite(parsedTotal) ? parsedTotal : 0
    if (total.value < list.value.length) {
      total.value = list.value.length
    }
    list.value.forEach((req: any) => loadTaskCount(req.id))
  } finally {
    loading.value = false
  }
}

async function loadMembers() {
  try {
    const res = await projectApi.getMembers(projectId)
    members.value = (res as any).data || []
  } catch {
    members.value = []
  }
}

async function loadSprints() {
  try {
    const res = await sprintApi.list({ projectId })
    const data = (res as any).data
    sprints.value = data?.records ?? data?.list ?? (Array.isArray(data) ? data : [])
  } catch {
    sprints.value = []
  }
}

async function loadTaskCount(reqId: number) {
  try {
    const res = await taskApi.list({ projectId, requirementId: reqId, page: 1, size: 1 })
    taskCountMap[reqId] = (res as any).data?.total ?? 0
  } catch {}
}

async function loadTasks(reqId: number) {
  taskLoadingMap[reqId] = true
  try {
    const res = await taskApi.list({ projectId, requirementId: reqId, page: 1, size: 100 })
    const data = (res as any).data
    taskMap[reqId] = data?.records || []
  } finally {
    taskLoadingMap[reqId] = false
  }
}

function onExpand(row: any, expandedRows: any[]) {
  if (expandedRows.some((r: any) => r.id === row.id)) loadTasks(row.id)
}

function openCreate() {
  if (!canManageProject.value) {
    ElMessage.warning(requirementText.messages.noManagePermission)
    return
  }
  showCreate.value = true
}

async function openEdit(row: any) {
  if (!canManageProject.value) {
    ElMessage.warning(requirementText.messages.noManagePermission)
    return
  }
  editingReq.value = row
  try {
    const res = await requirementApi.get(row.id)
    const d = (res as any).data
    editForm.title = d.title ?? ''
    editForm.description = d.description ?? ''
    editForm.priority = d.priority ?? 'MEDIUM'
    editForm.assigneeId = d.assigneeId ?? null
    editForm.sprintId = d.sprintId ?? null
    editForm.estimatedHours = d.estimatedHours ?? 0
    editForm.acceptanceCriteria = d.acceptanceCriteria ?? ''
    editForm.startDate = d.startDate ? String(d.startDate).slice(0, 10) : ''
    editForm.dueDate = d.dueDate ? String(d.dueDate).slice(0, 10) : ''
    showEdit.value = true
    await loadEditAttachments(row.id)
  } catch {
    ElMessage.error(requirementText.messages.loadRequirementFailed)
  }
}

async function loadEditAttachments(requirementId: number) {
  try {
    const res = await requirementApi.listAttachments(requirementId)
    editAttachments.value = (res as any).data ?? []
  } catch {
    editAttachments.value = []
  }
}

async function removeEditAttachment(att: any) {
  try {
    await ElMessageBox.confirm(requirementText.messages.confirmDeleteAttachment, requirementText.messages.confirmTitle, {
      type: 'warning'
    })
  } catch {
    return
  }
  if (!editingReq.value?.id) return
  try {
    await requirementApi.deleteAttachment(editingReq.value.id, att.id)
    ElMessage.success(requirementText.messages.deleted)
    editAttachments.value = editAttachments.value.filter(a => a.id !== att.id)
  } catch {
    ElMessage.error(requirementText.messages.deleteFailed)
  }
}

async function uploadEditAttachment(file: File) {
  if (!editingReq.value?.id) return
  editUploading.value = true
  try {
    const res = await requirementApi.uploadAttachment(editingReq.value.id, file)
    const newAtt = (res as any).data
    if (newAtt) editAttachments.value = [newAtt, ...editAttachments.value]
    ElMessage.success(requirementText.messages.uploadSuccess)
  } catch {
    ElMessage.error(requirementText.messages.uploadFailed)
  } finally {
    editUploading.value = false
  }
}

async function saveEdit() {
  await (editFormRef.value as any)?.validate().catch(() => {})
  if (!editForm.title.trim()) {
    ElMessage.warning(requirementText.messages.titleRequired)
    return
  }
  if (!editingReq.value?.id) return
  if (!canManageProject.value) {
    ElMessage.warning(requirementText.messages.noManagePermission)
    return
  }
  updating.value = true
  try {
    const payload = {
      title: editForm.title,
      description: editForm.description,
      priority: editForm.priority,
      assigneeId: editForm.assigneeId ?? undefined,
      sprintId: editForm.sprintId ?? undefined,
      estimatedHours: editForm.estimatedHours,
      acceptanceCriteria: editForm.acceptanceCriteria,
      startDate: editForm.startDate || undefined,
      dueDate: editForm.dueDate || undefined
    }
    await requirementApi.update(editingReq.value.id, payload)
    ElMessage.success(requirementText.messages.requirementUpdated)
    showEdit.value = false
    load()
  } finally {
    updating.value = false
  }
}


function openTaskCreate(req: any) {
  if (!canManageProject.value) {
    ElMessage.warning(requirementText.messages.noManagePermission)
    return
  }
  currentReq.value = req
  taskForm.title = ''
  taskForm.type = 'TASK'
  taskForm.priority = 'MEDIUM'
  taskForm.assigneeId = req?.assigneeId ?? null
  taskForm.estimatedHours = 0
  taskForm.dueDate = ''
  showTaskCreate.value = true
}

function cloneTaskTemplateRows(rows: TaskTemplateRow[]): TaskTemplateRow[] {
  return rows.map(row => ({ ...row }))
}

function applySelectedTaskTemplate() {
  const defaultAssigneeId = currentReq.value?.assigneeId ?? null
  taskTemplateRows.value = cloneTaskTemplateRows(taskTemplatePresets[taskTemplateKey.value] ?? []).map(row => ({
    ...row,
    assigneeId: defaultAssigneeId
  }))
}

function addTaskTemplateRow() {
  taskTemplateRows.value.push({
    title: '',
    type: 'TASK',
    priority: 'MEDIUM',
    assigneeId: currentReq.value?.assigneeId ?? null,
    estimatedHours: 0,
    dueDate: ''
  })
}

function removeTaskTemplateRow(index: number) {
  taskTemplateRows.value.splice(index, 1)
}

function openTaskTemplateDialog(req: any) {
  if (!canManageProject.value) {
    ElMessage.warning(requirementText.messages.noManagePermission)
    return
  }
  currentReq.value = req
  taskTemplateKey.value = 'feature'
  applySelectedTaskTemplate()
  showTaskTemplateDialog.value = true
}

async function submitTaskTemplate() {
  if (!currentReq.value?.id) {
    return
  }

  const validRows = taskTemplateRows.value
    .map(row => ({
      ...row,
      title: row.title.trim(),
      estimatedHours: Number.isFinite(Number(row.estimatedHours)) ? Number(row.estimatedHours) : 0
    }))
    .filter(row => row.title)

  if (!validRows.length) {
    ElMessage.warning(taskTemplateText.messages.titleRequired)
    return
  }

  if (validRows.some(row => row.estimatedHours < 0)) {
    ElMessage.warning(taskTemplateText.messages.estimatedInvalid)
    return
  }

  taskTemplateSubmitting.value = true
  try {
    let successCount = 0
    let failedCount = 0
    for (const row of validRows) {
      try {
        await taskApi.create({
          projectId,
          requirementId: currentReq.value.id,
          title: row.title,
          type: row.type,
          priority: row.priority,
          estimatedHours: row.estimatedHours,
          dueDate: row.dueDate || undefined,
          assigneeId: row.assigneeId ?? undefined
        })
        successCount += 1
      } catch {
        failedCount += 1
      }
    }

    if (failedCount === 0) {
      ElMessage.success(taskTemplateText.messages.createSuccess)
      showTaskTemplateDialog.value = false
    } else if (successCount > 0) {
      ElMessage.warning(`${taskTemplateText.messages.createPartialFailed} (${successCount}/${validRows.length})`)
    } else {
      ElMessage.error(taskTemplateText.messages.createFailed)
    }

    if (currentReq.value?.id) {
      loadTasks(currentReq.value.id)
      loadTaskCount(currentReq.value.id)
    }
    await load()
  } finally {
    taskTemplateSubmitting.value = false
  }
}

async function create() {
  await (formRef.value as any)?.validate().catch(() => {})
  if (!form.title.trim()) {
    ElMessage.warning(requirementText.messages.titleRequired)
    return
  }
  creating.value = true
  try {
    const res = await requirementApi.create({ ...form, projectId })
    const newId = (res as any).data?.id
    if (newId && uploadFileList.value?.length) {
      for (const item of uploadFileList.value) {
        if (item.raw) await requirementApi.uploadAttachment(newId, item.raw)
      }
      uploadFileList.value = []
    }
    ElMessage.success(requirementText.messages.requirementCreated)
    showCreate.value = false
    Object.assign(form, { title: '', description: '', priority: 'MEDIUM', estimatedHours: 0, acceptanceCriteria: '', startDate: '', dueDate: '' })
    load()
  } finally {
    creating.value = false
  }
}

async function createTask() {
  if (!taskForm.title.trim()) {
    ElMessage.warning(requirementText.messages.taskTitleRequired)
    return
  }
  creatingTask.value = true
  try {
    await taskApi.create({ ...taskForm, projectId, requirementId: currentReq.value?.id, assigneeId: taskForm.assigneeId ?? undefined })
    ElMessage.success(requirementText.messages.taskCreated)
    showTaskCreate.value = false
    if (currentReq.value?.id) {
      loadTasks(currentReq.value.id)
      loadTaskCount(currentReq.value.id)
    }
    load()
  } finally {
    creatingTask.value = false
  }
}

async function changeStatus(row: any, status: string) {
  if (!canEditRequirement(row)) {
    ElMessage.warning(requirementText.messages.noEditPermission)
    return
  }
  if (status === 'DONE') {
    openDoneStatusDialog(row)
    return
  }

  await requirementApi.updateStatus(row.id, status)
  ElMessage.success(requirementText.messages.statusUpdated)
  load()
}

function openDoneStatusDialog(row: any) {
  doneStatusTarget.value = row
  doneStatusForm.actualStartAt = ''
  doneStatusForm.actualEndAt = ''
  doneStatusForm.verificationScenario = ''
  doneStatusForm.verificationSteps = ''
  doneStatusForm.verificationResult = ''
  doneScreenshotFileList.value = []
  showDoneStatusDialog.value = true
}

function closeDoneStatusDialog() {
  showDoneStatusDialog.value = false
  doneStatusTarget.value = null
  doneStatusForm.actualStartAt = ''
  doneStatusForm.actualEndAt = ''
  doneStatusForm.verificationScenario = ''
  doneStatusForm.verificationSteps = ''
  doneStatusForm.verificationResult = ''
  doneScreenshotFileList.value = []
}

function beforeDoneScreenshotUpload(file: File) {
  if (!file.type || !file.type.startsWith('image/')) {
    ElMessage.warning(doneScreenshotText.screenshotTypeInvalid)
    return false
  }
  return false
}

async function submitDoneStatus() {
  if (!doneStatusTarget.value?.id) {
    return
  }
  if (!doneStatusForm.actualStartAt || !doneStatusForm.actualEndAt) {
    ElMessage.warning(requirementText.messages.doneTimeRequired)
    return
  }

  if (!doneStatusForm.verificationScenario.trim()
    || !doneStatusForm.verificationSteps.trim()
    || !doneStatusForm.verificationResult.trim()) {
    ElMessage.warning(doneRequiredMessage)
    return
  }

  const startAtMs = new Date(doneStatusForm.actualStartAt).getTime()
  const endAtMs = new Date(doneStatusForm.actualEndAt).getTime()
  if (!Number.isFinite(startAtMs) || !Number.isFinite(endAtMs) || endAtMs <= startAtMs) {
    ElMessage.warning(requirementText.messages.doneTimeRangeInvalid)
    return
  }

  doneStatusSubmitting.value = true
  try {
    await requirementApi.updateStatus(doneStatusTarget.value.id, 'DONE', {
      actualStartAt: doneStatusForm.actualStartAt,
      actualEndAt: doneStatusForm.actualEndAt,
      verificationScenario: doneStatusForm.verificationScenario.trim(),
      verificationSteps: doneStatusForm.verificationSteps.trim(),
      verificationResult: doneStatusForm.verificationResult.trim()
    })

    let screenshotUploadFailed = false
    for (const item of doneScreenshotFileList.value) {
      const raw = item?.raw as File | undefined
      if (!raw) continue
      try {
        await requirementApi.uploadAttachment(doneStatusTarget.value.id, raw)
      } catch {
        screenshotUploadFailed = true
      }
    }

    if (screenshotUploadFailed) {
      ElMessage.warning(doneScreenshotText.screenshotUploadPartialFailed)
    }

    ElMessage.success(requirementText.messages.statusUpdated)
    closeDoneStatusDialog()
    await load()
  } finally {
    doneStatusSubmitting.value = false
  }
}

onMounted(() => {
  void (async () => {
    await ensureProjectPermission()
    await Promise.all([load(), loadMembers(), loadSprints()])
  })()
})
</script>

<style scoped>
.req-page {
  padding: 0;
  min-height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-lg);
}

.title-block {
  display: flex;
  flex-direction: column;
  position: relative;
  padding-left: var(--space-md);
}

.title-block::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 3px;
  border-radius: var(--app-radius-pill);
  background: linear-gradient(180deg, var(--app-color-primary), var(--app-color-accent));
}

.view-toggles {
  display: flex;
  gap: var(--space-xs);
}

.view-toggles .el-button.is-circle {
  width: 32px;
  height: 32px;
}

/* Quick tabs */
.quick-tabs {
  display: flex;
  gap: var(--space-xs);
  margin-bottom: var(--space-lg);
  border: 1px solid var(--app-border-soft);
  border-radius: var(--app-radius-sm);
  padding: var(--space-xs);
  background: color-mix(in srgb, var(--app-bg-surface) 70%, transparent);
}

.quick-tab {
  padding: calc(var(--space-sm) + 2px) var(--space-lg);
  font-size: 13px;
  color: var(--app-text-secondary);
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  border-radius: var(--el-border-radius-small);
  transition: color 0.15s, background 0.15s, border-color 0.15s;
}

.quick-tab:hover {
  color: var(--app-text-primary);
  background: var(--app-bg-muted);
}

.quick-tab.active {
  color: var(--app-color-primary);
  font-weight: 600;
  background: color-mix(in srgb, var(--app-color-primary) 10%, transparent);
  border-bottom-color: var(--app-color-primary);
}

/* Filter bar */
.filter-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-md);
  margin-bottom: var(--space-lg);
}

.filter-search {
  width: 220px;
}

.filter-select {
  width: 140px;
}

.filter-bar-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}

.icon-btn {
  font-size: 16px;
  color: var(--app-text-secondary);
}

.icon-btn:hover {
  color: var(--app-text-primary);
}

.column-picker {
  padding: var(--space-xs) 0;
}

.column-picker-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--app-text-primary);
  margin-bottom: var(--space-md);
}

.column-picker-item {
  margin-bottom: var(--space-sm);
}

.column-picker-item:last-child {
  margin-bottom: 0;
}

.form-tip {
  font-size: 12px;
  color: var(--app-text-muted);
  margin-top: var(--space-xs);
  line-height: 1.4;
}

.full-width-control {
  width: 100%;
}

.upload-tip-spaced {
  margin-top: var(--space-xs);
}

.edit-attachments {
  width: 100%;
}

.attachment-list {
  margin-bottom: var(--space-md);
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-sm) var(--space-md);
  background: var(--app-bg-muted);
  border-radius: var(--el-border-radius-small);
  margin-bottom: var(--space-xs);
  font-size: 13px;
}

.attachment-item .att-icon {
  color: var(--app-text-muted);
  font-size: 16px;
}

.attachment-item .att-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--app-text-primary);
}

.attachment-item .att-size {
  color: var(--app-text-muted);
  font-size: 12px;
  flex-shrink: 0;
}

.detail-body {
  padding: 0 var(--space-xs);
}

.detail-section {
  margin-bottom: var(--space-lg);
}

.detail-section:last-of-type {
  margin-bottom: 0;
}

.detail-title {
  margin: 0 0 var(--space-md);
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text-primary);
  line-height: 1.4;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-bottom: var(--space-md);
}

.detail-actions {
  display: flex;
  justify-content: flex-start;
  margin-bottom: var(--space-sm);
}

.detail-type {
  font-size: 12px;
  color: var(--app-text-secondary);
}

.detail-desc {
  margin-bottom: var(--space-lg);
}

.detail-section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--app-text-secondary);
  margin-bottom: var(--space-md);
}

/* Table card */
.table-card {
  border-radius: var(--app-radius-sm);
  border: 1px solid var(--app-border-soft);
  overflow: hidden;
  transition: box-shadow 0.2s ease, border-color 0.2s ease;
}

.table-card:hover {
  border-color: color-mix(in srgb, var(--app-color-primary) 30%, var(--app-border-soft));
  box-shadow: var(--app-shadow-soft);
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.req-table {
  --el-table-border-color: var(--app-border-soft);
  --el-table-header-bg-color: var(--app-bg-muted);
}

.req-table :deep(.el-table__row:hover) {
  background-color: var(--app-bg-muted) !important;
}

.req-table :deep(.el-table th.el-table__cell) {
  font-weight: 600;
  font-size: 12px;
  color: var(--app-text-secondary);
  text-transform: none;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  min-width: 0;
}


.title-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--app-text-primary);
  font-size: 14px;
}

.pill {
  font-size: 12px;
}

.assignee-cell {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.assignee-cell .avatar {
  background: var(--app-border-soft);
  color: var(--app-text-secondary);
  font-size: 12px;
}

.assignee-name {
  font-size: 13px;
  color: var(--app-text-secondary);
}

.expand-wrap {
  padding: var(--space-lg) var(--space-xl) var(--space-xl);
  background: var(--app-bg-muted);
  border-radius: var(--el-border-radius-small);
  margin: var(--space-sm) var(--space-lg);
}

.expand-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-md);
}

.expand-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--app-text-secondary);
}

.expand-table {
  margin-top: var(--space-sm);
}

.expand-empty {
  padding: var(--space-lg) 0;
}

.hours {
  font-size: 12px;
  color: var(--app-text-secondary);
}

.readonly-hint {
  font-size: 12px;
  color: var(--app-text-muted);
}

.action-cell {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-xs);
  white-space: nowrap;
}

.action-status-btn :deep(.el-icon) {
  margin-left: 2px;
}

.template-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-sm);
  margin-bottom: var(--space-xs);
}

.template-select {
  width: 240px;
  max-width: 100%;
}

.template-table {
  margin-top: var(--space-sm);
}

.pagination-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--space-md);
  padding: var(--space-lg) var(--space-xl);
  border-top: 1px solid var(--app-border-soft);
  background: var(--app-bg-muted);
}

.pagination-summary {
  font-size: 13px;
  color: var(--app-text-secondary);
}

.pagination-wrap .el-pagination {
  justify-content: flex-end;
}

@media (max-width: 1439px) {
  .filter-bar {
    gap: var(--space-sm);
  }

  .filter-search {
    flex: 1 1 100%;
    width: 100%;
  }

  .filter-select {
    flex: 1 1 160px;
    width: 160px;
  }
}

@media (max-width: 1365px) {
  .filter-select {
    flex: 1 1 calc(50% - var(--space-sm));
    width: calc(50% - var(--space-sm));
    min-width: 0;
  }

  .filter-bar-right {
    width: 100%;
    margin-left: 0;
    justify-content: flex-end;
  }
}
</style>
