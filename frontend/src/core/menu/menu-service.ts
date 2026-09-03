import type { AxiosInstance } from 'axios'
import http from '../http/client'
import type { MenuItemResponse } from './menu'

class MenuService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async listar(): Promise<MenuItemResponse[]> {
    const { data } = await this.http.get('/menus')
    return data
  }
}

export default new MenuService(http)
