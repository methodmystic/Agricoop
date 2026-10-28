@echo off
git add .
git commit -m "update: deployment and vercel config"
git pull origin main --no-edit
git push origin main
npx vercel --prod
