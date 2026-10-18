import { use } from 'react'
import { NotificationContext } from './NotificationContext'

export function useNotify() {
  const notify = use(NotificationContext)
  if (!notify) {
    throw new Error(
      'useNotify muss innerhalb von NotificationProvider verwendet werden',
    )
  }
  return notify
}
