<template>
  <div class="dashboard">
    <div class="dashboard-main">
      <!-- 欢迎区域 -->
      <div class="welcome-section">
        <h2 class="welcome-text">欢迎回来！ admin</h2>
      </div>

      <!-- 数据概览卡片 -->
      <el-row :gutter="20" class="data-overview">
        <el-col :span="6" v-for="(item, index) in dataCards" :key="index">
          <el-card class="data-card">
            <div class="card-content">
              <div class="icon-wrapper" :class="item.color">
                <el-icon><component :is="item.icon" /></el-icon>
              </div>
              <div class="data-info">
                <div class="data-title">{{ item.title }}</div>
                <div class="data-value">
                  {{ item.value }}
                  <span class="unit">{{ item.unit }}</span>
                  <el-icon v-if="item.trend" class="trend-icon" :class="item.trendType">
                    <component :is="item.trend" />
                  </el-icon>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" class="chart-section">
        <el-col :span="16">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <span class="title">内容数据</span>
                </div>
                <div class="header-right">
                  <el-link type="primary" :underline="false" @click="handleViewMore">
                    查看更多
                    <el-icon class="el-icon--right">
                      <ArrowRight />
                    </el-icon>
                  </el-link>
                </div>
              </div>
            </template>
            <v-chart class="chart" :option="lineChartOption" autoresize />
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>内容类型占比</span>
              </div>
            </template>
            <v-chart class="pie-chart" :option="pieChartOption" autoresize />
          </el-card>
        </el-col>
      </el-row>

      <!-- 内容列表 -->
      <el-card class="content-list-card">
        <template #header>
          <div class="list-header">
            <span>线上热门内容</span>
            <el-link type="primary" :underline="false">查看更多</el-link>
          </div>
        </template>
        <div class="list-tabs">
          <el-tabs>
            <el-tab-pane label="文本" name="text" />
            <el-tab-pane label="图片" name="image" />
            <el-tab-pane label="视频" name="video" />
          </el-tabs>
        </div>
        <el-table :data="contentList" style="width: 100%">
          <el-table-column prop="rank" label="排名" width="80" />
          <el-table-column prop="title" label="内容标题" />
          <el-table-column prop="views" label="点击量" />
          <el-table-column prop="trend" label="日环比" width="120">
            <template #default="{ row }">
              <span :class="['trend', row.trend > 0 ? 'up' : 'down']">
                {{ Math.abs(row.trend) }}%
                <el-icon>
                  <component :is="row.trend > 0 ? 'CaretTop' : 'CaretBottom'" />
                </el-icon>
              </span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 右侧边栏 -->
    <div class="dashboard-right">
      <!-- 快捷操作 -->
      <el-card class="quick-actions">
        <template #header>
          <div class="card-header">
            <span>快捷操作</span>
            <span class="manage-text">管理</span>
          </div>
        </template>
        <div class="actions-grid">
          <div class="action-item" v-for="(item, index) in quickActions" :key="index" @click="handleQuickAction(item)">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.name }}</span>
          </div>
        </div>
      </el-card>

      <!-- 最近访问 -->
      <el-card class="recent-visits">
        <template #header>
          <div class="card-header">
            <span>最近访问</span>
          </div>
        </template>
        <div class="visits-grid">
          <div class="visit-item" v-for="(item, index) in recentVisits" :key="index">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.name }}</span>
          </div>
        </div>
      </el-card>

      <!-- 公告 -->
      <el-card class="announcement-card">
        <template #header>
          <div class="card-header">
            <span>公告</span>
            <el-link type="primary" :underline="false">查看更多</el-link>
          </div>
        </template>
        <div class="announcement-list">
          <div v-for="(item, index) in announcements" :key="index" class="announcement-item">
            <el-tag :type="item.type" size="small">{{ item.tag }}</el-tag>
            <span class="announcement-title">{{ item.title }}</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { LineChart, PieChart } from "echarts/charts";
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
} from "echarts/components";
import VChart from "vue-echarts";
import {
  Document,
  Folder,
  ChatDotRound,
  TrendCharts,
  CaretTop,
  CaretBottom,
  ArrowRight,
  View,
  User,
} from "@element-plus/icons-vue";
import { useRouter } from 'vue-router'
import { 
  getDashboardStats, 
  getTrend, 
  getHotPosts, 
  getContentDistribution,
  getVisitStats,
  getVisitTrend,
  type DashboardStats,
  type TrendData,
  type VisitStats,
  type VisitTrendData,
  type HotPostVO
} from "@/api/statistics";

use([
  CanvasRenderer,
  LineChart,
  PieChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
]);

const router = useRouter()

// 统计数据
const dashboardStats = ref<DashboardStats | null>(null);
const trendData = ref<TrendData | null>(null);
const hotPostsList = ref<HotPostVO[]>([]);
const contentDistributionData = ref<any[]>([]);
const visitStats = ref<VisitStats | null>(null);
const visitTrendData = ref<VisitTrendData | null>(null);
const loading = ref(false);

// 计算环比增长率
const commentGrowthRate = computed(() => {
  if (!dashboardStats.value) return 0;
  const { todayComments, yesterdayComments } = dashboardStats.value;
  if (yesterdayComments === 0) return todayComments > 0 ? 100 : 0;
  return ((todayComments - yesterdayComments) / yesterdayComments * 100).toFixed(1);
});

// 折线图配置
const lineChartOption = computed(() => ({
  tooltip: {
    trigger: "axis",
  },
  legend: {
    data: ['帖子', '用户', '评论'],
    bottom: 0,
  },
  grid: {
    left: "3%",
    right: "4%",
    bottom: "15%",
    containLabel: true,
  },
  xAxis: {
    type: "category",
    boundaryGap: false,
    data: trendData.value?.dates || [],
  },
  yAxis: {
    type: "value",
  },
  series: [
    {
      name: '帖子',
      data: trendData.value?.postCounts || [],
      type: "line",
      smooth: true,
      areaStyle: { opacity: 0.3 },
      lineStyle: { width: 2 },
      itemStyle: { color: '#409EFF' },
    },
    {
      name: '用户',
      data: trendData.value?.userCounts || [],
      type: "line",
      smooth: true,
      areaStyle: { opacity: 0.3 },
      lineStyle: { width: 2 },
      itemStyle: { color: '#67C23A' },
    },
    {
      name: '评论',
      data: trendData.value?.commentCounts || [],
      type: "line",
      smooth: true,
      areaStyle: { opacity: 0.3 },
      lineStyle: { width: 2 },
      itemStyle: { color: '#E6A23C' },
    },
  ],
}));

// 饼图配置
const pieChartOption = computed(() => ({
  tooltip: {
    trigger: "item",
  },
  legend: {
    orient: "horizontal",
    bottom: 0,
  },
  series: [
    {
      type: "pie",
      radius: ["40%", "70%"],
      avoidLabelOverlap: false,
      label: {
        show: true,
        position: "outside",
      },
      data: contentDistributionData.value.length > 0 
        ? contentDistributionData.value 
        : [
            { value: 0, name: "已发布" },
            { value: 0, name: "草稿" },
            { value: 0, name: "精选" },
          ],
    },
  ],
}));

// 数据卡片配置
const dataCards = computed(() => [
  {
    title: '在线用户',
    value: dashboardStats.value?.onlineUserCount || 0,
    unit: '人',
    icon: 'User',
    color: 'blue'
  },
  {
    title: '今日UV',
    value: visitStats.value?.todayUV || 0,
    unit: '人',
    icon: 'View',
    color: 'purple'
  },
  {
    title: '帖子总数',
    value: dashboardStats.value?.totalPosts || 0,
    unit: '篇',
    icon: 'Document',
    color: 'orange'
  },
  {
    title: '用户总数',
    value: dashboardStats.value?.totalUsers || 0,
    unit: '人',
    icon: 'Folder',
    color: 'cyan'
  }
]);

// 内容列表数据
const contentList = computed(() => {
  return hotPostsList.value.map((post, index) => ({
    rank: index + 1,
    title: post.title || '无标题',
    views: formatNumber(post.viewCount || 0),
    trend: post.growthRate || 0, // 使用后端返回的环比数据
  }));
});

// 格式化数字
function formatNumber(num: number): string {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w+';
  }
  return num.toString();
}

// 快捷操作数据
const quickActions = ref([
  { name: '帖子管理', icon: 'Document', path: '/content/post' },
  { name: '用户管理', icon: 'User', path: '/system/user' },
  { name: '评论管理', icon: 'ChatDotRound', path: '/content/comment' },
  { name: '标签管理', icon: 'CollectionTag', path: '/content/tag' },
  { name: '公告管理', icon: 'Bell', path: '/content/announcement' }
]);

// 处理快捷操作点击
const handleQuickAction = (item: any) => {
  router.push(item.path)
}

// 最近访问数据
const recentVisits = ref([
  { name: '帖子管理', icon: 'Document' },
  { name: '用户管理', icon: 'User' },
  { name: '系统设置', icon: 'Setting' },
]);

// 公告数据
const announcements = computed(() => {
  const items = [];
  if (dashboardStats.value?.pendingReports) {
    items.push({ type: 'warning', tag: '待处理', title: `${dashboardStats.value.pendingReports} 条举报待处理` });
  }
  if (dashboardStats.value?.pendingFeedbacks) {
    items.push({ type: 'info', tag: '反馈', title: `${dashboardStats.value.pendingFeedbacks} 条用户反馈待回复` });
  }
  if (dashboardStats.value?.todayPosts) {
    items.push({ type: 'primary', tag: '今日', title: `新增 ${dashboardStats.value.todayPosts} 篇帖子` });
  }
  if (dashboardStats.value?.todayUsers) {
    items.push({ type: 'success', tag: '今日', title: `新增 ${dashboardStats.value.todayUsers} 位用户` });
  }
  return items.length > 0 ? items : [{ type: 'info', tag: '提示', title: '暂无新消息' }];
});

// 加载数据
async function loadData() {
  loading.value = true;
  try {
    const [statsRes, trendRes, hotPostsRes, distributionRes, visitRes, visitTrendRes] = await Promise.all([
      getDashboardStats(),
      getTrend(7),
      getHotPosts(5),
      getContentDistribution(),
      getVisitStats(),
      getVisitTrend(7),
    ]);
    
    dashboardStats.value = statsRes.data;
    trendData.value = trendRes.data;
    hotPostsList.value = hotPostsRes.data || [];
    contentDistributionData.value = distributionRes.data || [];
    visitStats.value = visitRes.data;
    visitTrendData.value = visitTrendRes.data;
  } catch (error) {
    console.error('加载仪表盘数据失败:', error);
  } finally {
    loading.value = false;
  }
}

const handleViewMore = () => {
  router.push('/content/post');
}

onMounted(() => {
  loadData();
});
</script>

<style scoped lang="scss">
.dashboard {
  padding: 20px;
  display: flex;
  gap: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;

  .dashboard-main {
    flex: 1;
    min-width: 0; // 防止内容溢出

    .welcome-section {
      margin-bottom: 24px;
      .welcome-text {
        font-size: 24px;
        font-weight: 500;
        color: #303133;
        margin: 0;
      }
    }

    .data-overview {
      margin-bottom: 24px;

      .data-card {
        .card-content {
          display: flex;
          align-items: center;

          .icon-wrapper {
            width: 48px;
            height: 48px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 16px;

            .el-icon {
              font-size: 24px;
              color: #fff;
            }

            &.blue {
              background-color: #409eff;
            }
            &.purple {
              background-color: #906eff;
            }
            &.orange {
              background-color: #ff9900;
            }
            &.cyan {
              background-color: #19be6b;
            }
          }

          .data-info {
            .data-title {
              font-size: 14px;
              color: #909399;
              margin-bottom: 8px;
            }

            .data-value {
              font-size: 24px;
              font-weight: 500;
              color: #303133;
              display: flex;
              align-items: center;

              .unit {
                font-size: 14px;
                color: #909399;
                margin-left: 4px;
              }

              .trend-icon {
                margin-left: 8px;
                &.up {
                  color: #67c23a;
                }
                &.down {
                  color: #f56c6c;
                }
              }
            }
          }
        }
      }
    }

    .chart-section {
      margin-bottom: 24px;

      .chart-card {
        margin-bottom: 0;
        
        .chart,
        .pie-chart {
          height: 350px;
        }

        .card-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;

          .header-left {
            .title {
              font-size: 16px;
              font-weight: 500;
              color: #303133;
            }
          }

          .header-right {
            .el-link {
              font-size: 14px;
              display: flex;
              align-items: center;
              
              .el-icon {
                margin-left: 4px;
              }
            }
          }
        }
      }
    }

    .content-list-card {
      margin-bottom: 24px;
      .list-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .trend {
        display: flex;
        align-items: center;
        justify-content: flex-end;

        &.up {
          color: #67c23a;
        }

        &.down {
          color: #f56c6c;
        }

        .el-icon {
          margin-left: 4px;
        }
      }
    }
  }

  .dashboard-right {
    width: 300px;
    flex-shrink: 0;

    .quick-actions,
    .recent-visits,
    .announcement-card {
      margin-bottom: 20px;
      background-color: #fff;
      border-radius: 4px;

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .manage-text {
          color: #409EFF;
          cursor: pointer;
          font-size: 14px;
        }
      }
    }

    .actions-grid,
    .visits-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 12px;

      .action-item,
      .visit-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
        padding: 12px;
        cursor: pointer;

        &:hover {
          background-color: #f5f7fa;
          border-radius: 4px;
        }

        .el-icon {
          font-size: 20px;
          color: #409EFF;
        }

        span {
          font-size: 12px;
          color: #606266;
        }
      }
    }

    .announcement-list {
      .announcement-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 0;
        cursor: pointer;

        &:hover {
          .announcement-title {
            color: #409EFF;
          }
        }

        .announcement-title {
          font-size: 14px;
          color: #606266;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }
}
</style>
