export interface MenuItemResponse {
  id: string
  labelKey: string
  icon: string | null
  route: string | null
  children: MenuItemResponse[]
}
