import type { BaseResponse, PageResponse } from '@/utils/service'
import type { SpaceVO, SpaceQuery, SpaceDTO } from './type'

const areas = ['安静学习区', '电子阅览区', '研讨区', '多媒体区', '休闲阅读区']
const floors = ['1F', '2F', '3F', '4F']
const spaceTypes = ['SEAT', 'ROOM']

function generateSpaces(): SpaceVO[] {
  const list: SpaceVO[] = []
  let id = 1

  for (let f = 0; f < 4; f++) {
    for (let a = 0; a < areas.length; a++) {
      const count = spaceTypes[0] === 'SEAT' ? 5 : 2
      for (let i = 0; i < (a === 2 ? 2 : 5); i++) {
        const isRoom = a === 2
        const type = isRoom ? 'ROOM' : 'SEAT'
        const cap = isRoom ? [6, 8, 10, 12][i % 4] : 1
        const areaName = areas[a]
        const floor = floors[f]

        const config: any = {}
        if (isRoom) {
          config.projector = i % 2 === 0
          config.whiteboard = true
          config.network = 'wifi6'
          if (i === 0) config.videoConf = true
        } else {
          config.power = i % 3 !== 0
          config.light = i % 2 === 0 ? 'good' : 'normal'
          if (a === 3) { config.projector = true; config.audio = true }
          if (a === 4) config.sofa = true
        }

        list.push({
          id: id++,
          spaceName: isRoom
            ? `研讨室-${floor.replace('F', '')}0${i + 1}`
            : `${areaName.charAt(0)}区-${String(i + 1).padStart(2, '0')}号座`,
          spaceType: type,
          areaName,
          floor,
          capacity: cap,
          equipmentConfig: config,
          qrCode: '',
          imageUrl: '',
          description: isRoom
            ? `可容纳${cap}人的标准研讨室，配备${Object.keys(config).filter(k => config[k] === true || config[k]).join('、')}`
            : `${areaName}，${config.power ? '配备电源插座' : ''}${config.sofa ? '休闲沙发座位' : ''}`,
          useRules: isRoom
            ? '使用时间不超过10小时，请保持室内整洁，离开时关闭设备'
            : '使用时间不超过4小时，保持安静，手机调至静音',
          sortOrder: id,
          status: i % 7 === 0 ? 0 : i % 13 === 0 ? 2 : 1,
          createTime: '2026-06-01 00:00:00',
          updateTime: '2026-07-20 00:00:00',
        })
      }
    }
  }
  return list
}

const spaceList = generateSpaces()

const delay = () => new Promise((r) => setTimeout(r, 200 + Math.random() * 300))

export async function mockGetSpaceList(query: SpaceQuery): Promise<BaseResponse<PageResponse<SpaceVO>>> {
  await delay()
  let filtered = [...spaceList]

  if (query.spaceType) filtered = filtered.filter((s) => s.spaceType === query.spaceType)
  if (query.areaName) filtered = filtered.filter((s) => s.areaName === query.areaName)
  if (query.floor) filtered = filtered.filter((s) => s.floor === query.floor)
  if (query.status !== undefined) filtered = filtered.filter((s) => s.status === query.status)
  if (query.keyword) {
    const kw = query.keyword.toLowerCase()
    filtered = filtered.filter((s) => s.spaceName.includes(kw) || s.description?.includes(kw))
  }

  const page = query.page || 1
  const pageSize = query.pageSize || 10
  const start = (page - 1) * pageSize
  const list = filtered.slice(start, start + pageSize)

  return { code: 0, msg: 'ok', data: { total: filtered.length, list } }
}

export async function mockGetSpaceById(id: number): Promise<BaseResponse<SpaceVO>> {
  await delay()
  const space = spaceList.find((s) => s.id === id)
  if (!space) return { code: 404, msg: '空间不存在', data: null as any }
  return { code: 0, msg: 'ok', data: { ...space } }
}

export async function mockCreateSpace(data: SpaceDTO): Promise<BaseResponse<SpaceVO>> {
  await delay()
  const newSpace: SpaceVO = {
    id: spaceList.length + 1,
    spaceName: data.spaceName,
    spaceType: data.spaceType,
    areaName: data.areaName,
    floor: data.floor,
    capacity: data.capacity,
    equipmentConfig: data.equipmentConfig || {},
    qrCode: data.qrCode || '',
    imageUrl: data.imageUrl || '',
    description: data.description || '',
    useRules: data.useRules || '',
    sortOrder: data.sortOrder || 0,
    status: 1,
    createTime: new Date().toISOString(),
    updateTime: new Date().toISOString(),
  }
  spaceList.unshift(newSpace)
  return { code: 0, msg: '创建成功', data: newSpace }
}

export async function mockUpdateSpace(data: SpaceDTO): Promise<BaseResponse<null>> {
  await delay()
  const idx = spaceList.findIndex((s) => s.id === data.id)
  if (idx === -1) return { code: 404, msg: '空间不存在', data: null }
  Object.assign(spaceList[idx], data, { updateTime: new Date().toISOString() })
  return { code: 0, msg: '更新成功', data: null }
}

export async function mockDeleteSpace(id: number): Promise<BaseResponse<null>> {
  await delay()
  const idx = spaceList.findIndex((s) => s.id === id)
  if (idx === -1) return { code: 404, msg: '空间不存在', data: null }
  spaceList.splice(idx, 1)
  return { code: 0, msg: '删除成功', data: null }
}

export async function mockUpdateSpaceStatus(id: number, status: number): Promise<BaseResponse<null>> {
  await delay()
  const space = spaceList.find((s) => s.id === id)
  if (!space) return { code: 404, msg: '空间不存在', data: null }
  space.status = status
  return { code: 0, msg: '更新成功', data: null }
}

export { spaceList }
