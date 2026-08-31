<template>
  <v-chart class="nuvexa-line-chart" :option="option" :theme="paleta.nomeTema" autoresize />
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import VChart from 'vue-echarts'
import { useNuvexaChartPalette, corPorToken } from './nuvexa-echarts-theme'
import type { EChartsOption } from 'echarts'

export interface NuvexaLineChartPoint {
  label: string
  value: number
}

@Component({ name: 'NuvexaLineChart', components: { VChart } })
export default class NuvexaLineChart extends Vue {
  @Prop({ required: true })
  points!: NuvexaLineChartPoint[]

  @Prop({ default: 'primary' })
  color!: string

  get paleta() {
    return useNuvexaChartPalette()
  }

  get option(): EChartsOption {
    const cor = corPorToken(this.paleta.tema, this.color)
    return {
      grid: { left: 8, right: 8, top: 16, bottom: 24, containLabel: true },
      xAxis: {
        type: 'category',
        data: this.points.map((point) => point.label),
        boundaryGap: false,
        axisTick: { show: false },
      },
      yAxis: { type: 'value', show: false },
      tooltip: { trigger: 'axis' },
      series: [
        {
          type: 'line',
          data: this.points.map((point) => point.value),
          color: cor,
          symbolSize: 7,
          smooth: false,
          areaStyle: { color: cor, opacity: 0.15 },
        },
      ],
    }
  }
}
</script>

<style scoped>
.nuvexa-line-chart {
  width: 100%;
  height: 180px;
}
</style>
