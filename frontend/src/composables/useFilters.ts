import type { LocationQuery, Router } from 'vue-router'

export type FilterType = 'text' | 'enum' | 'multi-enum'

export interface FilterOption {
  value: string
  label: string
}

export interface FilterDef {
  label: string
  type: FilterType
  default?: string
  options?: FilterOption[]
}

export type FilterSchema = Record<string, FilterDef>

export interface ActiveChip {
  key: string
  label: string
  value: string
  rawValue: string
}

function toArray(raw: LocationQuery[string]): string[] {
  if (raw == null || raw === '') return []
  if (Array.isArray(raw)) return raw.filter((v): v is string => v != null)
  return String(raw).split(',').filter(Boolean)
}

function optionLabel(def: FilterDef, value: string): string {
  return def.options?.find((o) => o.value === value)?.label ?? value
}

/**
 * Estado de filtro sincronizado com a URL (query params). Sem dependência de
 * Composition API — chamado a partir de getters/métodos de componentes
 * class-based (vue-facing-decorator), que já são reativos a `$route.query`.
 */
export class FiltrosUrl<S extends FilterSchema> {
  constructor(
    private readonly getQuery: () => LocationQuery,
    private readonly router: Router,
    private readonly schema: S,
  ) {}

  value(key: keyof S & string): string {
    const def = this.schema[key]
    const raw = this.getQuery()[key]
    if (raw == null) return def.default ?? ''
    return Array.isArray(raw) ? (raw[0] ?? def.default ?? '') : String(raw)
  }

  values(key: keyof S & string): string[] {
    return toArray(this.getQuery()[key])
  }

  setValue(key: keyof S & string, value: string) {
    return this.applyQuery(key, value && value !== this.schema[key].default ? value : undefined)
  }

  setValues(key: keyof S & string, values: string[]) {
    return this.applyQuery(key, values.length ? values.join(',') : undefined)
  }

  toggleValue(key: keyof S & string, value: string) {
    const current = this.values(key)
    const next = current.includes(value) ? current.filter((v) => v !== value) : [...current, value]
    return this.setValues(key, next)
  }

  clearFilter(key: keyof S & string) {
    return this.applyQuery(key, undefined)
  }

  /**
   * Aplica vários campos de uma vez em um único replace de URL — usado pelo
   * botão "Pesquisar", que copia o rascunho inteiro para o aplicado de uma
   * só vez, em vez de um replace por campo.
   */
  applyMany(values: Partial<Record<keyof S & string, string | string[]>>) {
    const query = { ...this.getQuery() }
    for (const key of Object.keys(values)) {
      const def = this.schema[key]
      const value = values[key]
      const serialized =
        def.type === 'multi-enum'
          ? (value as string[]).length
            ? (value as string[]).join(',')
            : undefined
          : value && value !== def.default
            ? (value as string)
            : undefined
      if (serialized === undefined) delete query[key]
      else query[key] = serialized
    }
    delete query.page
    return this.router.replace({ query })
  }

  clearAll() {
    const query = { ...this.getQuery() }
    for (const key of Object.keys(this.schema)) delete query[key]
    delete query.page
    return this.router.replace({ query })
  }

  get activeChips(): ActiveChip[] {
    const chips: ActiveChip[] = []
    for (const key of Object.keys(this.schema)) {
      const def = this.schema[key]
      if (def.type === 'multi-enum') {
        for (const raw of this.values(key)) {
          chips.push({ key, label: def.label, value: optionLabel(def, raw), rawValue: raw })
        }
      } else {
        const raw = this.value(key)
        if (raw && raw !== def.default) {
          chips.push({ key, label: def.label, value: optionLabel(def, raw), rawValue: raw })
        }
      }
    }
    return chips
  }

  get activeCount(): number {
    return this.activeChips.length
  }

  removeChip(chip: ActiveChip) {
    const def = this.schema[chip.key]
    if (def.type === 'multi-enum') return this.toggleValue(chip.key, chip.rawValue)
    return this.clearFilter(chip.key)
  }

  private applyQuery(key: string, value: string | undefined) {
    const query = { ...this.getQuery() }
    if (value === undefined) delete query[key]
    else query[key] = value
    delete query.page
    return this.router.replace({ query })
  }
}

export function useFiltros<S extends FilterSchema>(
  vm: { $route: { query: LocationQuery }; $router: Router },
  schema: S,
): FiltrosUrl<S> {
  return new FiltrosUrl(() => vm.$route.query, vm.$router, schema)
}
