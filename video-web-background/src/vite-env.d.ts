/// <reference types="vite/client" />
declare module '*.vue' {
    import { DefineComponent } from 'vue'
    // 将 'vue' 文件视为 DefineComponent 类型
    const component: DefineComponent<{}, {}, any>
    export default component
   }
   