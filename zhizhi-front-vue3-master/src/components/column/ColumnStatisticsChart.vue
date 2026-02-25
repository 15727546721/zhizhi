<template>
  <div class="column-statistics">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>专栏数据统计</span>
          <el-radio-group v-model="days" size="small" @change="loadData">
            <el-radio-button :label="7">近7天</el-radio-button>
            <el-radio-button :label="30">近30天</el-radio-button>
            <el-radio-button :label="90">近90天</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div v-loading="loading" class="chart-container">
        <v-chart :option="chartOption" :autoresize="true" style="height: 400px" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
} from 'echarts/components'
import VChart from 'vue-echarts'
import { getColumnStatistics } from '@/api/column'
import { ElMessage } from 'element-plus'

use([
  CanvasRenderer,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
])

interface Props {
  columnId: number
}

const props = defineProps<Props>()

const days = ref(30)
const loading = ref(false)
const statisticsData = ref<any>(null)

const chartOption = computed(() => {
  if (!statisticsData.value) {
    return {}
  }

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
      },
    },
    legend: {
      data: ['阅读量', '订阅数'],
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: statisticsData.value.dates,
    },
    yAxis: [
      {
        type: 'value',
        name: '阅读量',
        position: 'left',
      },
      {
        type: 'value',
        name: '订阅数',
        position: 'right',
      },
    ],
    series: [
      {
        name: '阅读量',
        type: 'line',
        data: statisticsData.value.viewCounts,
        smooth: true,
        itemStyle: {
          color: '#409EFF',
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.05)' },
            ],
          },
        },
      },
      {
        name: '订阅数',
        type: 'line',
        yAxisIndex: 1,
        data: statisticsData.value.subscribeCounts,
        smooth: true,
        itemStyle: {
          color: '#67C23A',
        },
      },
    ],
  }
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getColumnStatistics(props.columnId, days.value)
    if (res.code === 20000) {
      statisticsData.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.column-statistics {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .chart-container {
    min-height: 400px;
  }
}
</style>
