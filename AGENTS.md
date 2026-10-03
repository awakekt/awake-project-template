# Agent guide

This project is a game built on [Awake](https://github.com/awakekt/awake). Rename the package
`com.awakekt.awake.template` to your own before you ship.

## Agent skill bundles

`.agents/skills.lock.toml` pins the game-authoring skills from
[awake-game-agent-skills](https://github.com/awakekt/awake-game-agent-skills) by tag, commit and
archive digest. Claude Code installs them at session start (`.claude/settings.json` runs
`hooks/sync-agent-skills.sh`). To install by hand:

```bash
git clone https://github.com/awakekt/awake-agent-skills .agents/vendor/awake-agent-skills-bootstrap
python3 .agents/vendor/awake-agent-skills-bootstrap/scripts/install_consumer.py --project .
```

Never edit the deployed copies under `.agents/` or `.claude/`. Move the pin with
`bump_lock.py` from the same bootstrap checkout.

Game rules, tuning values and art belong in this project as scene data; Awake Core supplies the
mechanisms. Before asking for a new engine feature, check the
[framework boundary](https://github.com/awakekt/awake/blob/main/docs/reference/framework-game-boundary.md).
