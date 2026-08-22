import vuetify from '../plugins/vuetify'

export interface ChartTheme {
  primary: string
  secondary: string
  success: string
  warning: string
  error: string
  textColor: string
  gridColor: string
}

export function getChartTheme(): ChartTheme {
  const colors = vuetify.theme.global.current.value.colors
  return {
    primary: String(colors.primary),
    secondary: String(colors.secondary),
    success: String(colors.success),
    warning: String(colors.warning),
    error: String(colors.error),
    textColor: String(colors['on-surface-variant']),
    gridColor: String(colors.outline),
  }
}
