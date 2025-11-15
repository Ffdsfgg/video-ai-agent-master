import {createRouter, createWebHashHistory} from "vue-router";

const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        // 登录
        {
            path: "/login",
            component: () => import("../pages/Login.vue")
        },

        //列表
        {
            path: "/admin",
            component: () => import("../pages/admin/Adminhome.vue"),
            children: [
                //平台概述
                {
                    path: "",
                    component:() =>  import("../pages/admin/AdminSummarize.vue"),
                },
                //用户列表
                {
                    path: "alluser",
                    component:() =>  import("../pages/admin/Alluser.vue"),
                },
                //视频列表
                {
                    path: "video",
                    component: () => import("../pages/video/Video.vue"),
                },
                //视频详情
                {
                    path: "videoInfo",
                    component: () => import("../pages/video/VideoInfo.vue"),
                },
                //字幕列表
                {
                    path: "subtitles",
                    component:() =>  import("../pages/video/Subtitles.vue"),
                },
                //知识库
                {
                    path: "repository",
                    component:() =>  import("../pages/config/Repository.vue"),
                },
                //我的详情
                {
                    path: "myinfo",
                    component:() =>  import("../pages/config/MyInfo.vue"),
                },
                //线程配置
                {
                    path: "thread",
                    component:() =>  import("../pages/config/ThreadPoolMonitor.vue"),
                }
            ],
        },
        {
            path: "/",
           redirect: "/login"
        },
    ],
});

export default router;
