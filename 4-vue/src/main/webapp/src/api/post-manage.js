import request from '@/util/request'

export function fetchPostSummary(authorName, category, page, show){
    var url = `/api/post-manage?page=${page}&show=${show}`
    if(authorName !== null){
        url += `&authorName=${authorName}`
    }
    if(category !== null){
        url += `&category=${category}`
    }
    return request({
        url: url,
        method: 'get'
    })
}

export function publishPost(id, status){
    return request({
        url: `/api/post-manage/${id}/published`,
        method: 'put',
        data: {
            'status': status
        }
    })
}

export function deletePost(id){
    return request({
        url: `/api/post-manage/${id}`,
        method: 'delete'
    })
}