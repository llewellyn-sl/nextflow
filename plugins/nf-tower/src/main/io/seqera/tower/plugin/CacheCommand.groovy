/*
 * Copyright 2013-2023, Seqera Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.seqera.tower.plugin

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.Session
import nextflow.SysEnv
import nextflow.cli.PluginAbstractExec
/**
 * Implements nextflow cache and restore commands
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
class CacheCommand implements PluginAbstractExec {

    List<String> getCommands() { ['cache-backup', 'cache-restore'] }

    @Override
    int exec(String cmd, List<String> args) {

        if( cmd == 'cache-backup') {
            cacheBackup()
            archiveLogs(session)
        }
        if( cmd == 'cache-restore' )
            cacheRestore()
        return 0
    }

    protected void cacheBackup() {
        log.debug "Running Nextflow cache backup"
        if( !SysEnv.get('NXF_CLOUDCACHE_PATH')) {
            // legacy cache manager
            new CacheManager(System.getenv()).saveCacheFiles()
        }
        else {
            new LogsHandler(getSession(), System.getenv()).saveFiles()
        }
    }

    protected void archiveLogs(Session sess) {
        // archive logs
        final archiver = TowerArchiver.create(sess, System.getenv())
        if( archiver ) try {
            log.debug "Running Nextflow logs archiver"
            archiver.archiveLogs()
        }
        finally {
            archiver.shutdown(sess)
        }
    }

    protected void cacheRestore() {
        if( !SysEnv.get('NXF_CLOUDCACHE_PATH')) {
            log.debug "Running Nextflow cache restore"
            // legacy cache manager
            new CacheManager(System.getenv()).restoreCacheFiles()
        }
        else {
            // this command is only kept for backward compatibility
            log.debug "Running Nextflow cache restore - DO NOTHING"
        }
    }

}
