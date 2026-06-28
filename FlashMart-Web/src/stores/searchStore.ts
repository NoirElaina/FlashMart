import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSearchStore = defineStore('search', () => {
    const keyword = ref('')
    function setKeyword(value: string) {
        keyword.value = value.trim()
    }
    function clearKeyword() {
        keyword.value = ''
    }
    return { setKeyword, clearKeyword, keyword }
})