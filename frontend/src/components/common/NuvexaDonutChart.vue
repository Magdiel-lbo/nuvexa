<template>
  <v-chart class="nuvexa-donut-chart" :option="option" :theme="paleta.nomeTema" autoresize />
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import VChart from 'vue-echarts'
import { useNuvexaChartPalette, corPorToken } from './nuvexa-echarts-theme'
import type { EChartsOption } from 'echarts'

export interface NuvexaDonutChartSlice {
  label: string
  value: number
  color?: string
}

const CORES_PADRAO = ['primary', 'secondary', 'success', 'warning', 'error']

@Component({ name: 'NuvexaDonutChart', components: { VChart } })
export default class NuvexaDonutChart extends Vue {
  @Prop({ required: true })
  slices!: NuvexaDonutChartSlice[]

  get paleta() {
    return useNuvexaChartPalette()
  }

  get option(): EChartsOption {
    const { tema } = this.paleta
    return {
      tooltip: { trigger: 'item' },
      legend: { bottom: 0, textStyle: { color: tema.textColor } },
      series: [
        {
          type: 'pie',
          radius: ['45%', '72%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: true,
          label: { color: tema.textColor, formatter: '{b}: {d}%' },
          labelLine: { lineStyle: { color: tema.gridColor } },
          data: this.slices.map((slice, index) => ({
            name: slice.label,
            value: slice.value,
            itemStyle: {
              color: corPorToken(tema, slice.color ?? CORES_PADRAO[index % CORES_PADRAO.length]),
              borderRadius: 4,
            },
          })),
        },
      ],
    }
  }
}
</script>

<style scoped>
.nuvexa-donut-chart {
  width: 100%;
  height: 260px;
}
</style>
