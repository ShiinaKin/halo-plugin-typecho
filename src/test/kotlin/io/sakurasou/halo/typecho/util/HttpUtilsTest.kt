package io.sakurasou.halo.typecho.util

import org.junit.jupiter.api.Test

/**
 * @author mashirot
 * 2024/5/9 18:51
 */
class HttpUtilsTest {
    @Test
    fun testListTags() {
        val pat = "pat_eyJraWQiOiJNU25QZDIza3NmWVI2MWlnenptSVFuM0hRdGtoY3RGSjJ0M0NZRlRqTjlFIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwOTAvIiwic3ViIjoibWFzaGlybyIsImlhdCI6MTcxNTI2MTc2NCwianRpIjoiMjljNTkxYTktNTdiNi1kZDA4LWI5NWMtODJlNzI3Zjk3NjIwIiwicGF0X25hbWUiOiJwYXQtbWFzaGlyby1BSkJoUSJ9.etinc6Z7LjDW2NSv1xtOMwVPBzg7_iGv_SsnfPeaqgzF9ylMBOgQUw-pUPdVTvRXo5q4-VuT1zlfdsiUnGLzEbzRYeqCHuZEyJGPMDRaG8WrkyEC2shm9iJCJR9lmox7uljoMQarTuuRcUpwu3kIKciZ4MlCWb52mBErvvP_EeliWjxlXczu1U2dbbnyG7ebHwdpH9NYLZQucMd0q7J69CdG4Rev53dRomQ4pd_zImt1Nbt_WohZmJfjAQfdClMaidV9s77JeBX9M3kKFayGE5WUWeDqLEbR8Yfw58WP9w1xLFQYXdDLkyaGtYyyRhEktF-dpu1T986tUJKnYpTbJKRi18GJ-jEPe8XgO6E2b_Zj5a3gz0SrjjVGqP0EXNxPodb2b3ijsh_23lvXw7qJA11AAT6hPY2P6i9MVfmSkvpV9lU7QzqxdHNrpVRm-8NUeAKqIoraW6Ol7isrTP7lPuQJhwLmEzhB2eDR4LqJ2Q0D_6kpCO3DPKU14jbklR6PIbo8L4-7bPp2NS8byseMVH_g1jZy25QOr3tROjJnmGOXkvR8v33IP3nTU8AqqMve2kaPaHJqGywrxTZpCrQjY5cKdnJLV7PxJQg2fZd5GBH4yO-uQqA9L0pYKEsg3JrOjQeHQLZwl8g4M4Uu24ooVCRXJu5AWfLbIB3coqcex18"
        val createTagUrl = "http://localhost:8090/apis/content.halo.run/v1alpha1/tags"
        val respJson = HttpUtils.sendGetReq(createTagUrl, pat)
        println(respJson)
    }
}