import { generateDashboardCharts, generateDashboardOverview } from '../mocks/dashboard-analytics.mock'
import type { DashboardCharts, DashboardFilters, DashboardOverview } from '../types/dashboard-analytics'

class DashboardService {
  async getOverview(filters: DashboardFilters): Promise<DashboardOverview> {
    return generateDashboardOverview(filters)
  }

  async getCharts(filters: DashboardFilters): Promise<DashboardCharts> {
    return generateDashboardCharts(filters)
  }
}

export default new DashboardService()
