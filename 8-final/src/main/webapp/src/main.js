import { createApp } from 'vue'
import App from '@/App.vue'
import router from '@/routes'

import 'bootstrap'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'blueimp-file-upload/css/jquery.fileupload-ui.css'
import 'blueimp-file-upload/css/jquery.fileupload.css'
import 'blueimp-file-upload/js/jquery.fileupload.js'
import 'jquery-ui/ui/widgets/datepicker.js'
import 'jquery-ui/ui/widget.js'

createApp(App)
    .use(router)
    .mount('#app')