import * as echarts from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { SVGRenderer } from 'echarts/renderers'
import { useThemeStore } from '../../store/theme.store'
import { getChartTheme, type ChartTheme } from '../../util/chart-theme'

// Registro modular: só os tipos de gráfico e componentes que os Nuvexa*Chart usam hoje,
// e o renderer SVG (mais leve/acessível que canvas para o volume de pontos do Dashboard).
echarts.use([BarChart, LineChart, PieChart, TooltipComponent, LegendComponent, GridComponent, SVGRenderer])

const NOME_BASE_TEMA = 'nuvexa'
const temasRegistrados = new Set<string>()

function construirTemaEcharts(tema: ChartTheme) {
  return {
    color: [tema.primary, tema.secondary, tema.success, tema.warning, tema.error],
    backgroundColor: 'transparent',
    textStyle: { color: tema.textColor },
    categoryAxis: {
      axisLine: { lineStyle: { color: tema.gridColor } },
      axisLabel: { color: tema.textColor },
      splitLine: { show: false },
    },
    valueAxis: {
      axisLine: { show: false },
      axisLabel: { color: tema.textColor },
      splitLine: { lineStyle: { color: tema.gridColor } },
    },
    tooltip: {
      backgroundColor: tema.gridColor,
      textStyle: { color: tema.textColor },
      borderWidth: 0,
    },
    legend: {
      textStyle: { color: tema.textColor },
    },
  }
}

export interface NuvexaChartPalette {
  nomeTema: string
  tema: ChartTheme
}

/**
 * Único ponto de leitura das cores do Vuetify para os gráficos. Um getter reativo ao
 * themeStore (não ao render) chama isto, então as custom properties de CSS só são lidas
 * quando o tema claro/escuro realmente muda, nunca a cada atualização de dado.
 */
export function useNuvexaChartPalette(): NuvexaChartPalette {
  const themeStore = useThemeStore()
  const nomeTema = `${NOME_BASE_TEMA}-${themeStore.name}`
  const tema = getChartTheme()
  if (!temasRegistrados.has(nomeTema)) {
    echarts.registerTheme(nomeTema, construirTemaEcharts(tema))
    temasRegistrados.add(nomeTema)
  }
  return { nomeTema, tema }
}

/** Resolve um token de cor ('primary'|'secondary'|'success'|'warning'|'error') para o valor atual do tema. */
export function corPorToken(tema: ChartTheme, token?: string): string {
  switch (token) {
    case 'secondary':
      return tema.secondary
    case 'success':
      return tema.success
    case 'warning':
      return tema.warning
    case 'error':
      return tema.error
    default:
      return tema.primary
  }
}
