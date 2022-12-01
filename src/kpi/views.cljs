(ns kpi.views
  (:require
   ["@supabase/auth-ui-react" :as supa-auth-ui]
   ["@supabase/supabase-js" :as supa]
   [re-frame.core :as re-frame]
   [re-com.core :as re-com :refer [at]]
   [kpi.styles :as styles]
   [kpi.config :as config]
   [kpi.events :as events]
   [kpi.routes :as routes]
   [kpi.subs :as subs]
   [kpi.secrets :as secrets]
   [applied-science.js-interop :as j]
   [potpuri.core :as pot]))

;; auth

;; probably should move this to fx or something
(def supabase-client
  (supa/createClient secrets/supabase-url secrets/supabase-anon-key))

(defn auth-internal [{:keys [supabase-client]} content]
  (let [state (supa-auth-ui/Auth.useUser)
        user  (j/get state :user)]
    (if (some? user)
      [content {:supabase/user user}]
      [:> supa-auth-ui/Auth {:supabase-client supabase-client}])))

(defn auth-wrapper [content]
 [:> supa-auth-ui/Auth.UserContextProvider {:supabase-client supabase-client}
   [:f> auth-internal {:supabase-client supabase-client}
    content]])

;; home

(defn home-title []
  (let [name (re-frame/subscribe [::subs/name])]
    [re-com/title
     :src   (at)
     :label (str "Hello from " @name ". This is the Home Page." " Git version " config/version)
     :level :level1
     :class (styles/level1)]))

(defn link-to-about-page []
  [re-com/hyperlink
   :src      (at)
   :label    "go to About Page"
   :on-click #(re-frame/dispatch [::events/navigate :about])])

(defn home-panel []
  [auth-wrapper
   (fn [{:supabase/keys [user]}]
     [re-com/v-box
      :src      (at)
      :gap      "1em"
      :children [[home-title]
                 [:div (str "User email is: " (-> user (j/get :email)))]
                 [link-to-about-page]]])])


(defmethod routes/panels :home-panel [] [home-panel])

;; about

(defn about-title []
  [re-com/title
   :src   (at)
   :label "This is the About Page."
   :level :level1])

(defn link-to-home-page []
  [re-com/hyperlink
   :src      (at)
   :label    "go to Home Page"
   :on-click #(re-frame/dispatch [::events/navigate :home])])

(defn about-panel []
  [re-com/v-box
   :src      (at)
   :gap      "1em"
   :children [[about-title]
              [link-to-home-page]]])

(defmethod routes/panels :about-panel [] [about-panel])

;; main

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [re-com/v-box
     :src      (at)
     :height   "100%"
     :children [(routes/panels @active-panel)]]))
