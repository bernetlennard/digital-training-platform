import { useCallback, useEffect, useState } from 'react'

interface LoadResult<T> {
  load: () => Promise<T>
  version: number
  data?: T
  error?: unknown
}

export interface ApiData<T> {
  data: T | undefined
  error: unknown
  isLoading: boolean
  reload: () => void
  setData: (update: (data: T) => T) => void
}

// load muss stabil sein (Funktion auf Modulebene oder useCallback), sonst lädt jedes Rendern neu
export function useApiData<T>(load: () => Promise<T>): ApiData<T> {
  const [version, setVersion] = useState(0)
  const [result, setResult] = useState<LoadResult<T>>()

  useEffect(() => {
    let ignore = false
    load().then(
      (data) => {
        if (!ignore) setResult({ load, version, data })
      },
      (error: unknown) => {
        if (!ignore) setResult({ load, version, error })
      },
    )
    return () => {
      ignore = true
    }
  }, [load, version])

  const reload = useCallback(() => setVersion((current) => current + 1), [])
  const setData = useCallback((update: (data: T) => T) => {
    setResult((current) => {
      if (current?.data === undefined) {
        return current
      }
      return { ...current, data: update(current.data) }
    })
  }, [])

  // Ergebnis einer anderen Anfrage (z. B. anderer Pfadparameter) nicht anzeigen
  const current = result?.load === load ? result : undefined
  return {
    data: current?.data,
    error: current?.error,
    isLoading: current?.version !== version,
    reload,
    setData,
  }
}
