<script setup lang="ts">
import {
  getJobPage,
  addJob,
  updateJob,
  runJob,
  changeStatus,
  deleteJob,
  infoJob,
} from "@/api/monitor/job";

defineOptions({
  name: "Job",
  inheritAttrs: false,
});

const router = useRouter();
const jobFormRef = ref(ElForm);
const rules = reactive({
  jobName: [{ required: true, message: "任务名称不能为空", trigger: "blur" }],
  invokeTarget: [
    { required: true, message: "调用目标字符串不能为空", trigger: "blur" },
  ],
  cronExpression: [
    { required: true, message: "cron执行表达式不能为空", trigger: "blur" },
  ],
});

const dialog = reactive({
  title: "",
  visible: false,
});

const formData = reactive<any>({});

const loading = ref(false);
const total = ref(0);
const ids = ref<number[]>([]);

const queryFormRef = ref(ElForm);
const queryParams = reactive<any>({
  pageNo: 1,
  pageSize: 10,
});

const openCron = ref(false);
const expression = ref("");
const openView = ref(false);

const tabelData = ref<any[]>();
const jobDictList = ref<any[]>();
const jobStatusList = ref<any[]>();
const jobMisfireList = ref<any[]>();
const jobDictDefaultValue = ref<any[]>();
const jobStatusDefaultValue = ref<any[]>();
const jobMisfireDefaultValue = ref<any[]>();

function getDictList() {
  // 字典功能暂不支持，使用静态数据
  jobDictList.value = [
    { label: "默认", value: "DEFAULT" },
    { label: "系统", value: "SYSTEM" }
  ];
  jobStatusList.value = [
    { label: "正常", value: "0" },
    { label: "暂停", value: "1" }
  ];
  jobMisfireList.value = [
    { label: "立即执行", value: "1" },
    { label: "执行一次", value: "2" },
    { label: "放弃执行", value: "3" }
  ];
  jobDictDefaultValue.value = "DEFAULT";
  jobStatusDefaultValue.value = "0";
  jobMisfireDefaultValue.value = "1";
}

/** cron表达式按钮操作 */
function handleShowCron() {
  expression.value = formData.cronExpression;
  openCron.value = true;
}
/** 确定后回传值 */
function crontabFill(value: any) {
  formData.cronExpression = value;
}

/** 查询 */
function handleQuery() {
  loading.value = true;
  getJobPage(queryParams)
    .then(({ data }) => {
      tabelData.value = data.data;
      total.value = data.total;
    })
    .finally(() => {
      loading.value = false;
    });
}
/** 重置查询 */
function resetQuery() {
  queryFormRef.value.resetFields();
  queryParams.pageNo = 1;
  handleQuery();
}

/** 重置表单 */
function resetForm() {
  jobFormRef.value.resetFields();
  jobFormRef.value.clearValidate();
  formData.jobId = undefined;
  formData.cronExpression = "";
  formData.invokeTarget = "";
  formData.jobName = "";
}

/** 关闭表单弹窗 */
function closeDialog() {
  dialog.visible = false;
  resetForm();
}

/** 打开表单弹窗 */
function openDialog(job?: Object) {
  dialog.visible = true;
  if (job) {
    dialog.title = "修改任务";
    Object.assign(formData, job);
  } else {
    dialog.title = "新增任务";
  }
}

/** 保存提交 */
function handleSubmit() {
  jobFormRef.value.validate((valid: any) => {
    if (valid) {
      loading.value = true;
      const jobId = formData.jobId;
      if (jobId) {
        updateJob(formData)
          .then(() => {
            ElMessage.success("修改成功");
            closeDialog();
            resetQuery();
          })
          .finally(() => (loading.value = false));
      } else {
        addJob(formData)
          .then(() => {
            ElMessage.success("新增成功");
            closeDialog();
            resetQuery();
          })
          .finally(() => (loading.value = false));
      }
    }
  });
}

//滑块切换
function handleStatusChange(row: any) {
  changeStatus(row).then(() => {
    ElMessage.success("修改成功");
  });
}

/** 删除 */
function handleDelete(jobId: any) {
  const jobIds = ref<any>([]);
  if (jobId) {
    jobIds.value.push(jobId);
  }
  if (ids.value.length) {
    jobIds.value = ids.value;
  }
  if (!jobIds.value) {
    ElMessage.warning("请勾选删除项");
    return;
  }
  ElMessageBox.confirm("确认删除已选中的数据项?", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    loading.value = true;
    deleteJob(jobIds.value)
      .then(() => {
        ElMessage.success("操作成功");
        handleQuery();
      })
      .finally(() => (loading.value = false));
  });
}

/** 行checkbox 选中事件 */
function handleSelectionChange(selection: any) {
  ids.value = selection.map((item: any) => item.jobId);
}

//选择cron表达式
function changeCron(val: any) {
  if (typeof val !== "string") return false;
  formData.cronExpression = val;
}

// 更多操作触发
function handleCommand(command: any, row: any) {
  switch (command) {
    case "handleRun":
      handleRunJob(row);
      break;
    case "handleView":
      handleJobInfo(row);
      break;
    default:
      break;
  }
}
//执行一次
function handleRunJob(row: any) {
  ElMessageBox.confirm(
    '确认要立即执行一次"' + row.jobName + '"任务吗？',
    "警告",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }
  ).then(() => {
    loading.value = true;
    runJob(row)
      .then(() => {
        ElMessage.success("操作成功");
      })
      .finally(() => (loading.value = false));
  });
}
//详情
function handleJobInfo(row: any) {
  infoJob(row.jobId).then((res) => {
    Object.assign(formData, res.data);
    openView.value = true;
  });
}
onMounted(() => {
  handleQuery();
  getDictList();
});
</script>

<template>
  <div class="app-container">
    <div class="content-wrapper">
      <!-- 搜索表单 -->
      <el-form ref="queryFormRef" :model="queryParams" class="search-form">
        <div class="form-row">
          <el-form-item label="任务名称" prop="jobName">
            <el-input
              v-model="queryParams.jobName"
              placeholder="请输入任务名称"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="任务组名" prop="jobGroup">
            <el-select
              v-model="queryParams.jobGroup"
              placeholder="请选择任务组名"
              clearable
            >
              <el-option
                v-for="dict in jobDictList"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="任务状态" prop="status">
            <el-select
              v-model="queryParams.status"
              placeholder="请选择任务状态"
              clearable
            >
              <el-option
                v-for="dict in jobStatusList"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <div class="btn-group">
            <el-button type="primary" @click="handleQuery">
              <i-ep-search />查询
            </el-button>
            <el-button @click="resetQuery">
              <i-ep-refresh />重置
            </el-button>
          </div>
        </div>
      </el-form>

      <div class="divider"></div>

      <!-- 表格区域 -->
      <div class="table-section">
        <div class="operation-bar">
          <div class="left">
            <el-button type="primary" @click="openDialog()" v-hasPerm="['system:job:add']">
              <i-ep-plus />新建
            </el-button>
            <el-button type="danger" :disabled="ids.length === 0" @click="handleDelete" v-hasPerm="['system:job:delete']">
              <i-ep-delete />批量删除
            </el-button>
          </div>
        </div>

        <el-table
        ref="dataTableRef"
        v-loading="loading"
        :data="tabelData"
        highlight-current-row
        stripe
        fit
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column
          label="任务名称"
          width="160"
          align="center"
          prop="jobName"
          :show-overflow-tooltip="true"
        />
        <el-table-column label="任务组名" align="center">
          <template #default="scope">
            <span v-for="(dict, index) in jobDictList" :key="index">
              <el-tag
                v-if="dict.value == scope.row.jobGroup"
                :type="dict.style"
              >
                {{ dict.label }}
              </el-tag>
            </span>
          </template>
        </el-table-column>
        <el-table-column
          label="调用目标字符串"
          align="center"
          prop="invokeTarget"
          :show-overflow-tooltip="true"
        />
        <el-table-column
          label="cron执行表达式"
          align="center"
          prop="cronExpression"
          :show-overflow-tooltip="true"
        />
        <el-table-column label="状态" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              active-value="0"
              inactive-value="1"
              v-hasPerm="['system:job:update']"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建者" prop="createBy" align="center" />
        <el-table-column
          label="创建时间"
          align="center"
          width="160"
          prop="createTime"
        />
        <el-table-column
          label="操作"
          align="center"
          class-name="small-padding fixed-width"
        >
          <template #default="scope">
            <el-button
              type="primary"
              link
              @click="openDialog(scope.row)"
              size="small"
              icon="Edit"
              v-hasPerm="['system:job:update']"
              >修改</el-button
            >
            <el-button
              type="danger"
              link
              @click="handleDelete(scope.row.jobId)"
              size="small"
              icon="Delete"
              v-hasPerm="['system:job:delete']"
              >删除</el-button
            >
            <el-dropdown
              @command="(command) => handleCommand(command, scope.row)"
              size="small"
            >
              <el-button type="primary" style="margin-left: 9px" link
                >更多
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    command="handleRun"
                    icon="CaretRight"
                    v-hasPerm="['system:job:run']"
                    >执行一次</el-dropdown-item
                  >
                  <el-dropdown-item command="handleView" icon="View"
                    >任务详细</el-dropdown-item
                  >
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

        <pagination
          v-if="total > 0"
          v-model:total="total"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="handleQuery"
        />
      </div>
    </div>

    <!-- 添加或修改定时任务对话框 -->
      <el-dialog
        :title="dialog.title"
        v-model="dialog.visible"
        width="800px"
        append-to-body
        @close="closeDialog"
      >
        <el-form
          ref="jobFormRef"
          :model="formData"
          :rules="rules"
          label-width="120px"
        >
          <el-row>
            <el-col :span="12">
              <el-form-item label="任务名称" prop="jobName">
                <el-input
                  v-model="formData.jobName"
                  placeholder="请输入任务名称"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="任务分组" prop="jobGroup">
                <el-select v-model="formData.jobGroup" placeholder="请选择">
                  <el-option
                    v-for="dict in jobDictList"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item prop="invokeTarget">
                <template #label>
                  调用方法
                  <el-tooltip placement="top">
                    <template #content>
                      Bean调用示例：blogQuartz.blogParams('blog')
                      <br />Class类调用示例：com.xu.quartz.BlogQuartz.blogParams('blog')
                      <br />参数说明：支持字符串，布尔类型，长整型，浮点型，整型
                    </template>
                    <el-icon s><QuestionFilled /></el-icon>
                  </el-tooltip>
                </template>
                <el-input
                  v-model="formData.invokeTarget"
                  placeholder="请输入调用目标字符串"
                />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="cron表达式" prop="cronExpression">
                <el-input
                  v-model="formData.cronExpression"
                  placeholder="请输入cron执行表达式"
                >
                  <template #append>
                    <el-button @click="handleShowCron">设置</el-button>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="错误策略" prop="misfirePolicy">
                <el-radio-group v-model="formData.misfirePolicy">
                  <el-radio-button
                    v-for="item in jobMisfireList"
                    :key="item.value"
                    :label="item.value"
                    >{{ item.label }}</el-radio-button
                  >
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="是否并发" prop="concurrent">
                <el-radio-group v-model="formData.concurrent">
                  <el-radio-button label="0">允许</el-radio-button>
                  <el-radio-button label="1">禁止</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="状态">
                <el-radio-group v-model="formData.status">
                  <el-radio
                    v-for="dict in jobStatusList"
                    :key="dict.value"
                    :label="dict.value"
                    >{{ dict.label }}</el-radio
                  >
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <template #footer>
          <el-button @click="dialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="handleSubmit">确 定</el-button>
        </template>
      </el-dialog>

      <!-- cron表达式 -->
      <el-dialog
        title="Cron表达式生成器"
        v-model="openCron"
        append-to-body
        destroy-on-close
      >
        <crontab
          ref="crontabRef"
          @hide="openCron = false"
          @fill="crontabFill"
          :expression="expression"
        />
      </el-dialog>

      <!-- 任务日志详细 -->
      <el-dialog
        title="任务详细"
        v-model="openView"
        width="700px"
        append-to-body
        @close="closeDialog"
      >
        <el-form ref="form" :model="formData" label-width="120px">
          <el-row>
            <el-col :span="12">
              <el-form-item label="任务编号：">{{
                formData.jobId
              }}</el-form-item>
              <el-form-item label="任务名称：">{{
                formData.jobName
              }}</el-form-item>
            </el-col>
            <el-col :span="12">
              <span v-for="item in jobDictList" :key="item.value">
                <el-form-item
                  label="任务分组："
                  v-if="item.value === formData.jobGroup"
                >
                  {{ item.label }}
                </el-form-item>
              </span>

              <el-form-item label="创建时间：">{{
                formData.createTime
              }}</el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="cron表达式：">{{
                formData.cronExpression
              }}</el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="下次执行时间：">{{
                formData.nextValidTime
              }}</el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="调用目标方法：">{{
                formData.invokeTarget
              }}</el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="任务状态：">
                <span v-for="item in jobStatusList" :key="item.value">
                  <div v-if="formData.status === item.value">
                    {{ item.label }}
                  </div>
                </span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="是否并发：">
                <div v-if="formData.concurrent === '0'">允许</div>
                <div v-else-if="formData.concurrent === '1'">禁止</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="执行策略：">
                <span v-for="item in jobMisfireList" :key="item.value">
                  <div v-if="formData.misfirePolicy === item.value">
                    {{ item.label }}
                  </div>
                </span>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <template #footer>
          <el-button @click="openView = false">关 闭</el-button>
        </template>
      </el-dialog>
  </div>
</template>
<style lang="scss" scoped>
::v-deep(.el-form-item__label) {
  align-items: center !important;
}
</style>
