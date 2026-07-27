export const SpaceTypeMap: Record<string, string> = {
  SEAT: '座位',
  ROOM: '研讨室',
}

export const SpaceTypeOptions = [
  { label: '全部', value: '' },
  { label: '座位', value: 'SEAT' },
  { label: '研讨室', value: 'ROOM' },
]

export const AreaNameMap: Record<string, string> = {
  QUIET_ZONE: '安静学习区',
  DIGITAL_ZONE: '电子阅览区',
  DISCUSSION_ZONE: '研讨区',
  MEDIA_ZONE: '多媒体区',
  READING_ZONE: '休闲阅读区',
}

export const AreaNameOptions = [
  { label: '全部', value: '' },
  { label: '安静学习区', value: '安静学习区' },
  { label: '电子阅览区', value: '电子阅览区' },
  { label: '研讨区', value: '研讨区' },
  { label: '多媒体区', value: '多媒体区' },
  { label: '休闲阅读区', value: '休闲阅读区' },
]

export const FloorOptions = [
  { label: '全部', value: '' },
  { label: '1F', value: '1F' },
  { label: '2F', value: '2F' },
  { label: '3F', value: '3F' },
  { label: '4F', value: '4F' },
]

export const SpaceStatusMap: Record<number, string> = {
  0: '维修中',
  1: '可用',
  2: '停用',
}

export const SpaceStatusOptions = [
  { label: '全部', value: '' },
  { label: '可用', value: 1 },
  { label: '维修中', value: 0 },
  { label: '停用', value: 2 },
]
