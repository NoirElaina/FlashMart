<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from 'lucide-vue-next'
import axios from 'axios'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { useUserStore } from '@/stores/userStore'

type AuthMode = 'login' | 'register'

interface ApiResponse<T> {
    code: number
    message: string
    data: T
}

interface AuthResponseData {
    token: string
    username: string
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const mode = ref<AuthMode>('login')
const isSubmitting = ref(false)
const errorMessage = ref('')

const loginForm = ref({ username: '', password: '' })
const registerForm = ref({ username: '', email: '', password: '', confirmPassword: '' })

const isLogin = computed(() => mode.value === 'login')

watch(mode, () => {
    errorMessage.value = ''
    loginForm.value = { username: '', password: '' }
    registerForm.value = { username: '', email: '', password: '', confirmPassword: '' }
})

function goBack() {
    router.push('/')
}

async function submit() {
    if (isSubmitting.value) return
    errorMessage.value = ''

    if (isLogin.value) {
        if (!loginForm.value.username.trim() || !loginForm.value.password.trim()) {
            errorMessage.value = '请输入用户名或邮箱，以及密码'
            return
        }
    } else {
        if (!registerForm.value.username.trim() || !registerForm.value.email.trim() || !registerForm.value.password.trim() || !registerForm.value.confirmPassword.trim()) {
            errorMessage.value = '请完整填写注册信息'
            return
        }
        if (registerForm.value.password !== registerForm.value.confirmPassword) {
            errorMessage.value = '两次输入的密码不一致'
            return
        }
    }

    isSubmitting.value = true
    try {
        const url = isLogin.value ? '/api/user/login' : '/api/user/register'
        const payload = isLogin.value
            ? { username: loginForm.value.username.trim(), password: loginForm.value.password.trim() }
            : { username: registerForm.value.username.trim(), email: registerForm.value.email.trim(), password: registerForm.value.password.trim() }

        const res = await axios.post<ApiResponse<AuthResponseData>>(url, payload)

        if (res.data.code !== 200) {
            errorMessage.value = res.data.message || (isLogin.value ? '登录失败' : '注册失败')
            return
        }

        const { token, username } = res.data.data
        userStore.setUser(token, username)

        const redirect = typeof route.query.redirect === 'string' && route.query.redirect.startsWith('/')
            ? route.query.redirect
            : '/'

        await router.push(redirect)
    } catch (error) {
        if (axios.isAxiosError<ApiResponse<null>>(error)) {
            errorMessage.value = error.response
                ? error.response.data?.message || `请求失败（${error.response.status}）`
                : '网络异常，无法连接服务器'
        } else {
            errorMessage.value = '未知错误'
        }
    } finally {
        isSubmitting.value = false
    }
}
</script>

<template>
    <section class="relative min-h-screen overflow-hidden bg-muted/30">
        <div class="auth-network" aria-hidden="true">
            <svg viewBox="0 0 1200 720" preserveAspectRatio="none">
                <g class="auth-network__lines">
                    <path d="M28 118 L152 86 L278 166 L420 96 L552 174 L700 112 L862 178 L1048 92 L1180 146" />
                    <path
                        d="M18 394 L132 310 L252 356 L372 250 L512 316 L640 242 L786 326 L916 256 L1088 318 L1190 244" />
                    <path d="M64 606 L188 512 L324 584 L438 472 L578 538 L706 438 L854 512 L982 418 L1150 486" />
                    <path d="M98 170 L184 304 L242 118 L382 242 L478 112 L596 266 L728 140 L842 286 L982 126" />
                    <path d="M238 680 L338 520 L452 634 L574 452 L694 602 L812 430 L930 568 L1110 382" />
                    <path
                        d="M0 244 L112 206 L226 276 L342 206 L470 264 L602 204 L720 274 L844 214 L998 282 L1200 194" />
                </g>
                <g class="auth-network__nodes">
                    <circle cx="152" cy="86" r="3.5" />
                    <circle cx="420" cy="96" r="3" />
                    <circle cx="700" cy="112" r="3.5" />
                    <circle cx="1048" cy="92" r="3" />
                    <circle cx="252" cy="356" r="4" />
                    <circle cx="512" cy="316" r="3" />
                    <circle cx="786" cy="326" r="3.5" />
                    <circle cx="1088" cy="318" r="4" />
                    <circle cx="188" cy="512" r="3.5" />
                    <circle cx="578" cy="538" r="4" />
                    <circle cx="854" cy="512" r="3" />
                    <circle cx="1110" cy="382" r="3.5" />
                </g>
            </svg>
        </div>

        <main class="relative z-10 container mx-auto flex min-h-screen items-center justify-center px-4 py-8">
            <div class="w-full max-w-md space-y-4">
                <Button type="button" variant="ghost" size="sm" class="gap-2 px-0" @click="goBack">
                    <ArrowLeft class="size-4" />
                    返回首页
                </Button>

                <Tabs v-model="mode" class="w-full">
                    <TabsList class="grid w-full grid-cols-2">
                        <TabsTrigger value="login">登录</TabsTrigger>
                        <TabsTrigger value="register">注册</TabsTrigger>
                    </TabsList>

                    <div class="auth-stage">
                        <div class="auth-card-layer auth-card-layer--login"
                            :class="{ 'auth-card-layer--active': isLogin }">
                            <Card>
                                <CardHeader>
                                    <CardTitle>登录你的账号</CardTitle>
                                    <CardDescription>输入你的账号信息，继续访问 FlashMart。</CardDescription>
                                </CardHeader>
                                <CardContent>
                                    <form class="grid gap-4" @submit.prevent="submit">
                                        <div class="grid gap-2">
                                            <Label for="login-username">用户名或邮箱</Label>
                                            <Input id="login-username" v-model="loginForm.username"
                                                placeholder="请输入用户名或邮箱" autocomplete="username" />
                                        </div>

                                        <div class="grid gap-2">
                                            <Label for="login-password">密码</Label>
                                            <Input id="login-password" v-model="loginForm.password" type="password"
                                                placeholder="请输入密码" autocomplete="current-password" />
                                        </div>

                                        <p v-if="errorMessage && isLogin"
                                            class="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">
                                            {{ errorMessage }}
                                        </p>

                                        <Button type="submit" :disabled="isSubmitting" class="w-full">
                                            {{ isSubmitting && isLogin ? '登录中...' : '登录' }}
                                        </Button>
                                    </form>
                                </CardContent>
                            </Card>
                        </div>

                        <div class="auth-card-layer auth-card-layer--register"
                            :class="{ 'auth-card-layer--active': !isLogin }">
                            <Card>
                                <CardHeader>
                                    <CardTitle>注册新账号</CardTitle>
                                    <CardDescription>填写基础信息，创建你的 FlashMart 账号。</CardDescription>
                                </CardHeader>
                                <CardContent>
                                    <form class="grid gap-4" @submit.prevent="submit">
                                        <div class="grid gap-2">
                                            <Label for="register-username">用户名</Label>
                                            <Input id="register-username" v-model="registerForm.username"
                                                placeholder="请设置用户名" autocomplete="username" />
                                        </div>

                                        <div class="grid gap-2">
                                            <Label for="register-email">邮箱</Label>
                                            <Input id="register-email" v-model="registerForm.email" type="email"
                                                placeholder="请输入邮箱" autocomplete="email" />
                                        </div>

                                        <div class="grid gap-2">
                                            <Label for="register-password">密码</Label>
                                            <Input id="register-password" v-model="registerForm.password"
                                                type="password" placeholder="请设置密码" autocomplete="new-password" />
                                        </div>

                                        <div class="grid gap-2">
                                            <Label for="register-confirm-password">确认密码</Label>
                                            <Input id="register-confirm-password" v-model="registerForm.confirmPassword"
                                                type="password" placeholder="请再次输入密码" autocomplete="new-password" />
                                        </div>

                                        <p v-if="errorMessage && !isLogin"
                                            class="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">
                                            {{ errorMessage }}
                                        </p>

                                        <Button type="submit" :disabled="isSubmitting" class="w-full">
                                            {{ isSubmitting && !isLogin ? '注册中...' : '注册' }}
                                        </Button>
                                    </form>
                                </CardContent>
                            </Card>
                        </div>
                    </div>
                </Tabs>
            </div>
        </main>
    </section>
</template>

<style scoped>
.auth-stage {
    position: relative;
    min-height: 32rem;
    margin-top: 1rem;
}

.auth-card-layer {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    opacity: 0;
    pointer-events: none;
    transition:
        opacity 0.2s ease,
        transform 0.24s cubic-bezier(0.22, 1, 0.36, 1);
}

.auth-card-layer>* {
    width: 100%;
}

.auth-card-layer--login {
    transform: translateX(-14px) scale(0.99);
}

.auth-card-layer--register {
    transform: translateX(14px) scale(0.99);
}

.auth-card-layer--active {
    opacity: 1;
    pointer-events: auto;
    transform: translateX(0) scale(1);
}

.auth-network {
    position: absolute;
    inset: 0;
    color: var(--muted-foreground);
    opacity: 0.28;
    pointer-events: none;
}

.auth-network::before {
    content: '';
    position: absolute;
    inset: 18% 24%;
    border-radius: 999px;
    background: color-mix(in oklch, var(--background) 78%, transparent);
    filter: blur(54px);
}

.auth-network svg {
    position: absolute;
    inset: -7% -4%;
    width: 108%;
    height: 114%;
}

.auth-network__lines path {
    fill: none;
    stroke: currentColor;
    stroke-width: 1.2;
    stroke-linecap: round;
    stroke-linejoin: round;
    stroke-dasharray: 8 18;
    animation: network-drift 16s linear infinite;
}

.auth-network__lines path:nth-child(2n) {
    animation-direction: reverse;
    animation-duration: 21s;
}

.auth-network__lines path:nth-child(3n) {
    stroke-dasharray: 4 14;
    animation-duration: 25s;
}

.auth-network__nodes circle {
    fill: currentColor;
    opacity: 0.55;
    animation: node-pulse 3.8s ease-in-out infinite;
}

.auth-network__nodes circle:nth-child(2n) {
    animation-delay: 0.8s;
}

.auth-network__nodes circle:nth-child(3n) {
    animation-delay: 1.6s;
}

@keyframes network-drift {
    from {
        stroke-dashoffset: 0;
        transform: translate3d(0, 0, 0);
    }

    50% {
        transform: translate3d(10px, -7px, 0);
    }

    to {
        stroke-dashoffset: -180;
        transform: translate3d(0, 0, 0);
    }
}

@keyframes node-pulse {

    0%,
    100% {
        opacity: 0.22;
        transform: scale(0.85);
    }

    45% {
        opacity: 0.7;
        transform: scale(1.2);
    }
}
</style>
