<template>
  <v-chart class="nuvexa-bar-chart" :option="option" :theme="paleta.nomeTema" autoresize />
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import VChart from 'vue-echarts'
import { useNuvexaChartPalette, corPorToken } from './nuvexa-echarts-theme'
import type { EChartsOption } from 'echarts'

export interface NuvexaBarChartRow {
  label: string
  value: number
  color?: string
}

@Component({ name: 'NuvexaBarChart', components: { VChart } })
export default class NuvexaBarChart extends Vue {
  @Prop({ required: true })
  rows!: NuvexaBarChartRow[]

  get paleta() {
    return useNuvexaChartPalette()
  }

  get option(): EChartsOption {
    const { tema } = this.paleta
    return {
      grid: { left: '2%', right: '14%', top: 8, bottom: 8, containLabel: true },
      xAxis: { type: 'value', axisLabel: { show: false }, splitLine: { show: false } },
      yAxis: { type: 'category', data: this.rows.map((row) => row.label), inverse: true },
      tooltip: { trigger: 'item' },
      series: [
        {
          type: 'bar',
          barMaxWidth: 18,
          label: { show: true, position: 'right', color: tema.textColor },
          data: this.rows.map((row) => ({
            value: row.value,
            itemStyle: { color: corPorToken(tema, row.color) },
          })),
        },
      ],
    }
  }
}
</script>

<style scoped>
.nuvexa-bar-chart {
  width: 100%;
  height: 200px;
}
</style>
