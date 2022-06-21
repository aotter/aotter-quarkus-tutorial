import axios from 'axios'

const service = axios.create({
    baseURL: '/',
    timeout: 5000
})

service.interceptors.response.use(
    response => {
        return response.data
    },
    error => {
        if(error.response.data.title === 'Constraint Violation'){
            Promise.reject(error)
        }
        alert(error.response.data.message)
        if(error.response.status === 401){
            window.location.href = '/login'
        }
    }

)

export default service